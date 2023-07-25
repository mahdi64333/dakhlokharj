package ir.demoodite.dakhlokharj.ui.components.mainactivity

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.data.settings.SettingsDataStore
import ir.demoodite.dakhlokharj.databinding.ActivityMainBinding
import ir.demoodite.dakhlokharj.utils.LocaleHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        setupNavigation()
        handleDrawerBackPressed()
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}