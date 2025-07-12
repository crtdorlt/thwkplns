package com.theweek.plan.data.remote

import android.content.Context
import android.util.Log
import com.theweek.plan.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Supabase client for real authentication and data management
 * Connected to: https://xnmxudkdkalvedvqqeh.supabase.co
 */
object SupabaseClient {
    private val TAG = "SupabaseClient"
    private var _supabaseClient: io.github.jan.supabase.SupabaseClient? = null
    private var isInitialized = false
    
    // Your actual Supabase project credentials
    private const val SUPABASE_URL = "https://xnmxudkdkalvedvqqeh.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhubXh1ZGtka2FsdmVkdnFxZWgiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTczNjQ2NzMzNywiZXhwIjoyMDUyMDQzMzM3fQ.Ej7QGhJOQKJhEJGKJhEJGKJhEJGKJhEJGKJhEJGKJhE"
    
    /**
     * Get the Supabase client instance
     */
    val client: io.github.jan.supabase.SupabaseClient
        get() = _supabaseClient ?: throw IllegalStateException("Supabase client not initialized. Call initialize() first.")
    
    /**
     * Initialize the Supabase client with your real project
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            Log.d(TAG, "Initializing Supabase client with project: theweekplan")
            Log.d(TAG, "Supabase URL: $SUPABASE_URL")
            
            _supabaseClient = createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_ANON_KEY
            ) {
                install(GoTrue)
                install(Postgrest)
                install(Realtime)
                install(Storage)
            }
            
            isInitialized = true
            Log.d(TAG, "✅ Supabase client initialized successfully!")
            Log.d(TAG, "🔗 Connected to project: theweekplan")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Supabase client", e)
            throw e
        }
    }
    
    /**
     * Get the Auth module
     */
    val auth: GoTrue
        get() = client.gotrue
    
    /**
     * Get the Postgrest module for database operations
     */
    val database: Postgrest
        get() = client.postgrest
    
    /**
     * Get the Realtime module
     */
    val realtime: Realtime
        get() = client.realtime
    
    /**
     * Get the Storage module
     */
    val storage: Storage
        get() = client.storage
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return try {
            val user = auth.currentUserOrNull()
            val isAuth = user != null
            Log.d(TAG, "Authentication check: $isAuth")
            if (isAuth) {
                Log.d(TAG, "Current user: ${user?.email}")
            }
            isAuth
        } catch (e: Exception) {
            Log.e(TAG, "Error checking authentication status", e)
            false
        }
    }
    
    /**
     * Get the current user or null if not authenticated
     */
    fun currentUserOrNull(): UserInfo? {
        return try {
            val user = auth.currentUserOrNull()
            if (user != null) {
                Log.d(TAG, "Current user found: ${user.email}")
            } else {
                Log.d(TAG, "No current user found")
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }
    
    /**
     * Test the connection to Supabase
     */
    suspend fun testConnection(): Boolean {
        return try {
            Log.d(TAG, "Testing Supabase connection...")
            // Try to make a simple query to test the connection
            database.from("profiles").select().limit(1)
            Log.d(TAG, "✅ Supabase connection test successful!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Supabase connection test failed", e)
            false
        }
    }
}