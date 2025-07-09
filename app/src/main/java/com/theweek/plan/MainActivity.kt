package com.theweek.plan

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.databinding.ActivityMainBinding
import com.theweek.plan.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize user repository
        userRepository = UserRepository(this)
        
        // Check if user is authenticated
        checkAuthStatus()
        
        // Load theme preference
        val sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up the top app bar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.title = getString(R.string.app_name)

        val navView: BottomNavigationView = binding.bottomNavigation
        
        // Get the NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup the bottom navigation with the nav controller
        navView.setupWithNavController(navController)

        // Setup the FAB to navigate to the add task fragment
        val fab: FloatingActionButton = binding.fabAddTask
        fab.setOnClickListener {
            navController.navigate(R.id.navigation_add_edit_task)
        }

        // Only show FAB on planning screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_tasks -> fab.show()
                else -> fab.hide()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Settings option removed as functionality is now in the Profile screen
        return super.onOptionsItemSelected(item)
    }
    
    /**
     * Check if the user is authenticated and redirect to login if not
     */
    private fun checkAuthStatus() {
        lifecycleScope.launch {
            try {
                if (!userRepository.isAuthenticated()) {
                    navigateToLogin()
                }
            } catch (e: Exception) {
                // If there's an error checking auth (e.g., Supabase not configured), go to login
                navigateToLogin()
            }
        }
    }
    
    /**
     * Navigate to login screen
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
