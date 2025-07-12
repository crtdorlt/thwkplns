package com.theweek.plan

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.theweek.plan.data.AppDatabase

class TheWeekPlanApp : Application() {

    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }

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
    }
}