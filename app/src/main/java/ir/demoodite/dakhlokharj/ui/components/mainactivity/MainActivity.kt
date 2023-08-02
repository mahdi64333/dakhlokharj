package ir.demoodite.dakhlokharj.ui.components.mainactivity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.databinding.ActivityMainBinding
import ir.demoodite.dakhlokharj.eventsystem.file.FileEventChannel
import ir.demoodite.dakhlokharj.eventsystem.file.FileEventType
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
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
    private var pendingFile: File? = null
    private val createAndSaveFileActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    savePendingFileToUri(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageSettingDataCollection()
        setupNavigation()
        handleDrawerBackPressed()
        startEventSystem()
    }

    private fun startEventSystem() {
        lifecycleScope.launch {
            FileEventChannel.getReceiver().collectLatest {
                when (it.type) {
                    FileEventType.SAVE_FILE -> saveFile(it.file)
                }
            }
        }
    }

    private fun saveFile(file: File) {
        pendingFile = file
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3"
            putExtra(Intent.EXTRA_TITLE, file.name)
        }

        createAndSaveFileActivityResultLauncher.launch(intent)
    }

    private fun savePendingFileToUri(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outputStream ->
                    outputStream.write(pendingFile?.readBytes())
                    Snackbar.make(
                        binding.coordinatorLayout,
                        getString(
                            R.string.saved_successfully
                        ),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                binding.coordinatorLayout,
                getString(R.string.operation_failed),
                Snackbar.LENGTH_LONG
            ).show()
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

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)

        // Fragments in navigation drawer
        val rootDestinations = setOf(
            R.id.homeFragment,
        )
        appBarConfiguration = AppBarConfiguration(
            rootDestinations,
            binding.drawerLayout
        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, arguments ->
            binding.AppBarLayout.isGone = arguments?.getBoolean("fullScreen", false) == true
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}