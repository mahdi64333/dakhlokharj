package ir.demoodite.dakhlokharj.ui.components.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.databinding.ActivityMainBinding
import ir.demoodite.dakhlokharj.ui.components.databasemanager.DatabaseManagerFragment
import ir.demoodite.dakhlokharj.ui.components.databasemanager.DatabaseManagerFragmentArgs
import ir.demoodite.dakhlokharj.ui.components.home.HomeFragment
import ir.demoodite.dakhlokharj.ui.showcase.ShowcaseStatus
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageSettingDataCollection()
        listenForUiFeedbackRequests()
        setupNavigation()
        handleDrawerBackPressed()
        showShowcaseIfNotShown()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            handleIntent(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupLanguageSettingDataCollection() {
        runBlocking {
            LocaleHelper.applicationLanguageCode = settingsDataStore.getLanguageFlow().first()
        }

        lifecycleScope.launch {
            settingsDataStore.getLanguageFlow().collectLatest {
                LocaleHelper.applicationLanguageCode = it
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun listenForUiFeedbackRequests() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Loading alert dialog declaring for later showing or dismissing
                val loadingAlertDialog =
                    SweetAlertDialog(this@MainActivity, SweetAlertDialog.PROGRESS_TYPE)

                getUiFeedbackReceiver().debounce(100).collectLatest { (type, messageResId) ->
                    when (type) {
                        FeedbackType.ERROR_DIALOG -> {
                            UiUtil.setSweetAlertDialogNightMode(resources)
                            SweetAlertDialog(
                                this@MainActivity, SweetAlertDialog.ERROR_TYPE
                            ).apply {
                                titleText = getString(R.string.an_error_has_occurred)
                                contentText = getString(messageResId)
                                confirmText = getString(R.string.confirm)
                                setConfirmClickListener { dismiss() }
                                show()
                                UiUtil.fixSweetAlertDialogButton(
                                    getButton(SweetAlertDialog.BUTTON_CONFIRM)
                                )
                            }
                        }
                        FeedbackType.SNACKBAR -> {
                            Snackbar.make(
                                binding.root, messageResId, Snackbar.LENGTH_LONG
                            ).show()
                        }
                        FeedbackType.START_LOADING -> loadingAlertDialog.show()
                        FeedbackType.STOP_LOADING -> loadingAlertDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)

        // Root distinctions which navigation drawer is openable within them
        val rootDestinations = setOf(
            R.id.homeFragment,
        )

        appBarConfiguration = AppBarConfiguration(
            rootDestinations, binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            // Hiding the AppBarLayout when a navigation destination has fullscreen argument
            binding.AppBarLayout.isGone =
                arguments?.getBoolean("fullScreen", false) == true
            // Locking the DrawerLayout in non root destination which shouldn't open the drawer
            binding.drawerLayout.setDrawerLockMode(
                if (rootDestinations.contains(destination.id)) LOCK_MODE_UNLOCKED
                else LOCK_MODE_LOCKED_CLOSED
            )
        }

        // Closing the navigation drawer when selecting an item
        binding.navigationView.setNavigationItemSelectedListener {
            binding.drawerLayout.close()
            it.onNavDestinationSelected(navController)
        }
    }

    private fun handleDrawerBackPressed() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isOpen) {
                    binding.drawerLayout.close()
                }
            }
        })
    }

    /**
     * Shows the [MainActivity] and [HomeFragment]'s showcases if they have not been shown.
     */
    private fun showShowcaseIfNotShown() {
        val showcaseStatus = ShowcaseStatus(this)

        if (navController.currentDestination?.id == R.id.homeFragment && !showcaseStatus.isShowcaseShown(
                ShowcaseStatus.Screen.HOME
            )
        ) {
            GuideView.Builder(this).setContentText(getString(R.string.showcase_resident_adding))
                .setDismissType(DismissType.anywhere).setTargetView(binding.toolbar.getChildAt(1))
                .setPointerType(PointerType.arrow)
                .setContentTypeFace(ResourcesCompat.getFont(this, R.font.iran_sans))
                .setContentTextSize(15)
                .setGuideListener {
                    lifecycleScope.launch {
                        HomeFragment.startShowcase()
                    }
                }
                .build()
                .show()
        }
    }

    private fun handleIntent(intent: Intent) {
        val fileSchemes = listOf("file", "content")
        val sqliteMimeTypes = listOf(
            "application/octet-stream", "application/x-sqlite3", "application/vnd.sqlite3"
        )

        intent.data?.let { intentData ->
            if (intent.action == Intent.ACTION_VIEW && fileSchemes.contains(intent.data?.scheme)) {
                if (sqliteMimeTypes.contains(contentResolver.getType(intentData))) {
                    if (navController.currentDestination?.id == R.id.databaseManagerFragment) {
                        lifecycleScope.launch {
                            DatabaseManagerFragment.importArchiveFromUri(intentData)
                        }
                    } else {
                        val args =
                            DatabaseManagerFragmentArgs.Builder()
                                .setImportingArchiveUri(intentData)
                                .build()
                        navController.navigate(R.id.databaseManagerFragment, args.toBundle())
                    }
                }
            }
        }
    }

    /**
     * An event system for receiving Ui event requests such as showing an error
     * or displaying a message. It makes showing ui events from viewModel much easier.
     */
    companion object UiFeedbackChannel {
        @Volatile
        private var INSTANCE: Channel<Pair<FeedbackType, Int>>? = null

        private fun getUiFeedbackReceiver(): Flow<Pair<FeedbackType, Int>> {
            return getUiFeedbackChannel().receiveAsFlow()
        }

        private fun getUiFeedbackChannel(): Channel<Pair<FeedbackType, Int>> {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Channel()
                }
                INSTANCE!!
            }
        }

        /**
         * Shows an error with a [SweetAlertDialog].
         *
         * @param errorRes String resource id of the error message
         */
        suspend fun sendError(@StringRes errorRes: Int) {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.ERROR_DIALOG, errorRes))
            }
        }

        /**
         * Shows a messsage with a [Snackbar].
         *
         * @param messageRes String resource id of the message
         */
        suspend fun sendMessage(@StringRes messageRes: Int) {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.SNACKBAR, messageRes))
            }
        }

        /**
         * Starts to show an indefinite loading [SweetAlertDialog].
         */
        suspend fun startLoading() {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.START_LOADING, 0))
            }
        }

        /**
         * Dismisses the previous loading [SweetAlertDialog].
         */
        suspend fun stopLoading() {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.STOP_LOADING, 0))
            }
        }

        enum class FeedbackType {
            ERROR_DIALOG, SNACKBAR, START_LOADING, STOP_LOADING,
        }
    }
}