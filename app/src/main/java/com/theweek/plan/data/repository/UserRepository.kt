package com.theweek.plan.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.theweek.plan.data.remote.SupabaseClient
import com.theweek.plan.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Repository for handling user authentication and profile management with Supabase
 */
class UserRepository(private val context: Context) {
    private val TAG = "UserRepository"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    @Serializable
    data class ProfileRow(
        val id: String,
        val email: String,
        val display_name: String? = null,
        val avatar_url: String? = null,
        val prefers_dark_mode: Boolean = false,
        val week_starts_on: Int = 1,
        val reminder_time: String? = null,
        val last_sync_timestamp: Long = 0,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    /**
     * Sign up a new user with email and password
     */
    suspend fun signUp(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Signing up user with email: $email")
            
            val result = SupabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = result.user?.id ?: throw Exception("User ID not found after signup")
            
            // Create user profile in database
            createUserProfile(UserProfile(
                id = userId,
                email = email
            ))
            
            Log.d(TAG, "User signed up successfully: $userId")
            Result.success(userId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error signing up user", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Signing in user with email: $email")
            
            val result = SupabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = result.user?.id ?: throw Exception("User ID not found after signin")
            
            Log.d(TAG, "User signed in successfully: $userId")
            Result.success(userId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in user", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Signing out user")
            
            SupabaseClient.auth.signOut()
            
            // Clear any cached data
            sharedPreferences.edit().clear().apply()
            
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out user", e)
            Result.failure(e)
        }
    }

    /**
     * Reset password for a user
     */
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Resetting password for email: $email")
            
            SupabaseClient.auth.resetPasswordForEmail(email)
            
            Log.d(TAG, "Password reset email sent successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting password", e)
            Result.failure(e)
        }
    }

    /**
     * Create a user profile in the database
     */
    private suspend fun createUserProfile(userProfile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating user profile for user: ${userProfile.id}")
            
            val profileRow = ProfileRow(
                id = userProfile.id,
                email = userProfile.email,
                display_name = userProfile.displayName,
                avatar_url = userProfile.avatarUrl,
                prefers_dark_mode = userProfile.prefersDarkMode,
                week_starts_on = userProfile.weekStartsOn,
                reminder_time = userProfile.reminderTime,
                last_sync_timestamp = userProfile.lastSyncTimestamp
            )
            
            SupabaseClient.database.from("profiles").insert(profileRow)
            
            Log.d(TAG, "User profile created successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Get the current user's profile
     */
    suspend fun getUserProfile(): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            val currentUser = SupabaseClient.currentUserOrNull()
            if (currentUser == null) {
                Log.d(TAG, "No authenticated user found")
                return@withContext Result.success(null)
            }
            
            Log.d(TAG, "Getting user profile for user: ${currentUser.id}")
            
            val response = SupabaseClient.database
                .from("profiles")
                .select()
                .eq("id", currentUser.id)
                .decodeSingle<ProfileRow>()
            
            val userProfile = UserProfile(
                id = response.id,
                email = response.email,
                displayName = response.display_name ?: "",
                avatarUrl = response.avatar_url,
                prefersDarkMode = response.prefers_dark_mode,
                weekStartsOn = response.week_starts_on,
                reminderTime = response.reminder_time,
                lastSyncTimestamp = response.last_sync_timestamp
            )
            
            Log.d(TAG, "User profile retrieved successfully")
            Result.success(userProfile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update the user profile
     */
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating user profile for user: ${userProfile.id}")
            
            val profileRow = ProfileRow(
                id = userProfile.id,
                email = userProfile.email,
                display_name = userProfile.displayName,
                avatar_url = userProfile.avatarUrl,
                prefers_dark_mode = userProfile.prefersDarkMode,
                week_starts_on = userProfile.weekStartsOn,
                reminder_time = userProfile.reminderTime,
                last_sync_timestamp = userProfile.lastSyncTimestamp
            )
            
            SupabaseClient.database
                .from("profiles")
                .update(profileRow)
                .eq("id", userProfile.id)
            
            Log.d(TAG, "User profile updated successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Get the authentication state as a Flow
     */
    fun getAuthState(): Flow<Boolean> = flow {
        try {
            val isAuth = SupabaseClient.isAuthenticated()
            emit(isAuth)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking auth state", e)
            emit(false)
        }
    }

    /**
     * Check if the user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return try {
            SupabaseClient.isAuthenticated()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking authentication", e)
            false
        }
    }
}