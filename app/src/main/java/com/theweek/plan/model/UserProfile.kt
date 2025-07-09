package com.theweek.plan.model

/**
 * Represents a user profile in the application
 */
data class UserProfile(
    val id: String, // UUID from Supabase Auth
    val email: String,
    val displayName: String = "",
    val avatarUrl: String? = null,
    val prefersDarkMode: Boolean = false,
    val weekStartsOn: Int = 1, // 0 = Sunday, 1 = Monday, etc.
    val reminderTime: String? = null, // Format: "HH:mm"
    val lastSyncTimestamp: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)