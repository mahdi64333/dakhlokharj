package ir.demoodite.dakhlokharj.ui.components.mainactivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
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
import ir.demoodite.dakhlokharj.ui.components.databasemanager.DatabaseManagerFragmentArgs
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import ir.demoodite.dakhlokharj.utils.UiUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
    private val fileSchemes = listOf("file", "content")
    private val sqliteMimeTypes = listOf(
        "application/octet-stream", "application/x-sqlite3", "application/vnd.sqlite3"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageSettingDataCollection()
        listenForUiFeedbackRequests()
        setupNavigation()
        handleDrawerBackPressed()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            handleIntent(intent)
        }
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
                                getButton(SweetAlertDialog.BUTTON_CONFIRM).apply {
                                    UiUtil.fixSweetAlertDialogButtons(this)
                                }
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

        // Fragments in navigation drawer
        val rootDestinations = setOf(
            R.id.homeFragment,
        )
        appBarConfiguration = AppBarConfiguration(
            rootDestinations, binding.drawerLayout
        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            binding.AppBarLayout.isGone = arguments?.getBoolean("fullScreen", false) == true
            binding.drawerLayout.setDrawerLockMode(
                if (rootDestinations.contains(destination.id)) LOCK_MODE_UNLOCKED
                else LOCK_MODE_LOCKED_CLOSED
            )
        }

        binding.navigationView.setNavigationItemSelectedListener {
            binding.drawerLayout.close()
            it.onNavDestinationSelected(navController)
        }
    }

    private fun handleDrawerBackPressed() {
        val onBackPressedDrawerCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isOpen) {
                    binding.drawerLayout.close()
                }
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedDrawerCallback)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                onBackPressedDrawerCallback.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                onBackPressedDrawerCallback.isEnabled = false
            }

            override fun onDrawerStateChanged(newState: Int) {}

        })
    }

    private fun handleIntent(intent: Intent) {
        intent.data?.let { intentData ->
            if (intent.action == Intent.ACTION_VIEW
                && fileSchemes.contains(intent.data?.scheme)
                && sqliteMimeTypes.contains(contentResolver.getType(intentData))
            ) {
                if (navController.currentDestination?.id == R.id.databaseManagerFragment) {
                    navController.popBackStack()
                }
                val args =
                    DatabaseManagerFragmentArgs.Builder()
                        .setImportingArchiveUri(intentData)
                        .build()
                navController.navigate(R.id.databaseManagerFragment, args.toBundle())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object UiFeedbackChannel {
        @Volatile
        private var INSTANCE: Channel<Pair<FeedbackType, Int>>? = null

        private fun getUiFeedbackReceiver(): Flow<Pair<FeedbackType, Int>> {
            return INSTANCE?.receiveAsFlow() ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Channel()
                }
                INSTANCE!!.receiveAsFlow()
            }
        }

        private fun getUiFeedbackChannel(): Channel<Pair<FeedbackType, Int>> {
            return INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Channel()
                }
                INSTANCE!!
            }
        }

        suspend fun sendError(@StringRes errorRes: Int) {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.ERROR_DIALOG, errorRes))
            }
        }

        suspend fun sendMessage(@StringRes messageRes: Int) {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.SNACKBAR, messageRes))
            }
        }

        suspend fun startLoading() {
            withContext(Dispatchers.Main) {
                getUiFeedbackChannel().send(Pair(FeedbackType.START_LOADING, 0))
            }
        }

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