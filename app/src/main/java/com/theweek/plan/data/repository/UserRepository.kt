package com.theweek.plan.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.theweek.plan.data.remote.SupabaseClient
import com.theweek.plan.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/**
 * Repository for handling user authentication and profile management
 * This is a simplified version for demonstration purposes
 */
class UserRepository(private val context: Context) {
    private val TAG = "UserRepository"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    /**
     * Sign up a new user with email and password
     * This is a simplified version for demonstration purposes
     */
    suspend fun signUp(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would sign up a user with Supabase
            Log.d(TAG, "Signing up user with email: $email")
            
            // Generate a mock user ID
            val userId = "user-${System.currentTimeMillis()}"
            
            // Create a mock user profile
            createUserProfile(UserProfile(
                id = userId,
                email = email
            ))
            
            // Store the user ID in SharedPreferences to maintain login state
            sharedPreferences.edit().putString("user_id", userId).apply()
            
            Result.success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing up user", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     * This is a simplified version for demonstration purposes
     */
    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would sign in a user with Supabase
            Log.d(TAG, "Signing in user with email: $email")
            
            // Generate a mock user ID
            val userId = "user-${System.currentTimeMillis()}"
            
            // Store the user ID in SharedPreferences to maintain login state
            sharedPreferences.edit().putString("user_id", userId).apply()
            
            Result.success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in user", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out the current user
     * This is a simplified version for demonstration purposes
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would sign out a user from Supabase
            Log.d(TAG, "Signing out user")
            
            // Clear the user ID from SharedPreferences to log out
            sharedPreferences.edit().remove("user_id").apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out user", e)
            Result.failure(e)
        }
    }

    /**
     * Reset password for a user
     * This is a simplified version for demonstration purposes
     */
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would reset a user's password with Supabase
            Log.d(TAG, "Resetting password for email: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting password", e)
            Result.failure(e)
        }
    }

    /**
     * Create a user profile in the database
     * This is a simplified version for demonstration purposes
     */
    private suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would create a user profile in Supabase
            Log.d(TAG, "Creating user profile for user: ${userProfile.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Get the current user's profile
     * This is a simplified version for demonstration purposes
     */
    suspend fun getUserProfile(): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would fetch a user's profile from Supabase
            Log.d(TAG, "Getting user profile")
            
            // Return a mock user profile for demonstration purposes
            val mockProfile = UserProfile(
                id = "user-123",
                email = "user@example.com",
                displayName = "Demo User",
                prefersDarkMode = true,
                weekStartsOn = 1
            )
            
            Result.success(mockProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update the user profile
     * This is a simplified version for demonstration purposes
     */
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would update a user's profile in Supabase
            Log.d(TAG, "Updating user profile for user: ${userProfile.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Get the authentication state as a Flow
     * This is a simplified version for demonstration purposes
     */
    fun getAuthState(): Flow<Boolean> {
        // Return a flow that emits the current authentication state
        return flowOf(sharedPreferences.getString("user_id", null) != null)
    }

    /**
     * Check if the user is authenticated
     * This is a simplified version for demonstration purposes
     */
    suspend fun isAuthenticated(): Boolean {
        // For demonstration purposes, check if we have a stored user ID
        val userId = sharedPreferences.getString("user_id", null)
        return userId != null
    }
}
