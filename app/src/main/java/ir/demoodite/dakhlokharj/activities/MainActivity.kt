package ir.demoodite.dakhlokharj.activities

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import ir.demoodite.dakhlokharj.R
import ir.demoodite.dakhlokharj.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        handleDrawerBackPressed()
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