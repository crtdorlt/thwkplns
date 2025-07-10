package com.theweek.plan

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.theweek.plan.data.AppDatabase
import com.theweek.plan.data.remote.SupabaseClient
import com.theweek.plan.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TheWeekPlanApp : Application() {

    private val TAG = "TheWeekPlanApp"
    
    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Sync manager for Supabase synchronization
    private lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "🚀 Starting TheWeekPlan App...")
        
        // Initialize dark mode based on saved preference
        val isDarkMode = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        // Initialize Supabase with real connection
        initializeSupabase()
        
        // Initialize sync manager
        initializeSyncManager()
        
        Log.d(TAG, "✅ TheWeekPlan App initialized successfully!")
    }
    
    /**
     * Initialize Supabase client with real project connection
     */
    private fun initializeSupabase() {
        try {
            Log.d(TAG, "🔗 Connecting to Supabase project: theweekplan")
            SupabaseClient.initialize(this)
            
            // Test the connection
            applicationScope.launch {
                val connectionTest = SupabaseClient.testConnection()
                if (connectionTest) {
                    Log.d(TAG, "✅ Successfully connected to Supabase!")
                } else {
                    Log.e(TAG, "❌ Failed to connect to Supabase")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing Supabase", e)
        }
    }
    
    /**
     * Initialize the sync manager
     */
    private fun initializeSyncManager() {
        try {
            Log.d(TAG, "🔄 Initializing sync manager...")
            syncManager = SyncManager(this)
            syncManager.initialize()
            Log.d(TAG, "✅ Sync manager initialized!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing sync manager", e)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        if (::syncManager.isInitialized) {
            syncManager.cleanup()
        }
        Log.d(TAG, "👋 TheWeekPlan App terminated")
    }
}