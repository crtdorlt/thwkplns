package com.theweek.plan

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.theweek.plan.data.AppDatabase
import com.theweek.plan.data.remote.SupabaseClient
import com.theweek.plan.data.sync.SyncManager
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TheWeekPlanApp : Application() {

    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Sync manager for Supabase synchronization
    private lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize dark mode based on saved preference
        val isDarkMode = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        // Initialize Supabase
        initializeSupabase()
        
        // Copy .env file to app's files directory if it doesn't exist
        copyEnvFile()
        
        // Initialize sync manager
        initializeSyncManager()
    }
    
    /**
     * Initialize Supabase client
     */
    private fun initializeSupabase() {
        try {
            SupabaseClient.initialize(this)
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }
    
    /**
     * Copy .env file from assets to app's files directory
     */
    private fun copyEnvFile() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                val envFile = getFileStreamPath(".env")
                if (!envFile.exists()) {
                    assets.open(".env").use { input ->
                        openFileOutput(".env", MODE_PRIVATE).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Initialize the sync manager
     */
    private fun initializeSyncManager() {
        syncManager = SyncManager(this)
        syncManager.initialize()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        if (::syncManager.isInitialized) {
            syncManager.cleanup()
        }
    }
}
