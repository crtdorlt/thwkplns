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
import io.github.cdimascio.dotenv.dotenv

/**
 * Supabase client for real authentication and data management
 */
object SupabaseClient {
    private val TAG = "SupabaseClient"
    private var _supabaseClient: io.github.jan.supabase.SupabaseClient? = null
    private var isInitialized = false
    
    /**
     * Get the Supabase client instance
     */
    val client: io.github.jan.supabase.SupabaseClient
        get() = _supabaseClient ?: throw IllegalStateException("Supabase client not initialized. Call initialize() first.")
    
    /**
     * Initialize the Supabase client
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            // Try to load from .env file first, fallback to BuildConfig
            val dotenv = try {
                dotenv {
                    directory = context.filesDir.absolutePath
                    filename = ".env"
                }
            } catch (e: Exception) {
                null
            }
            
            val supabaseUrl = dotenv?.get("SUPABASE_URL") ?: BuildConfig.SUPABASE_URL
            val supabaseKey = dotenv?.get("SUPABASE_ANON_KEY") ?: BuildConfig.SUPABASE_ANON_KEY
            
            if (supabaseUrl.isEmpty() || supabaseKey.isEmpty() || 
                supabaseUrl == "your_supabase_project_url" || 
                supabaseKey == "your_supabase_anon_key") {
                throw IllegalStateException("Supabase credentials not configured properly. Please check your .env file or BuildConfig.")
            }
            
            _supabaseClient = createSupabaseClient(
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey
            ) {
                install(GoTrue)
                install(Postgrest)
                install(Realtime)
                install(Storage)
            }
            
            isInitialized = true
            Log.d(TAG, "Supabase client initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Supabase client", e)
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
            auth.currentUserOrNull() != null
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
            auth.currentUserOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }
}