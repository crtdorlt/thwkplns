package com.theweek.plan.ui.settings

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.theweek.plan.R
import com.theweek.plan.databinding.ActivitySettingsBinding
import com.theweek.plan.databinding.DialogStreakLevelsBinding
import com.theweek.plan.ui.statistics.StreakLevel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the top app bar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // Load current theme setting
        val sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        
        // Set the switch state based on current theme
        binding.switchDarkMode.isChecked = isDarkMode
        
        // Set up the dark mode switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save the preference
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()
            
            // Apply the theme change
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, getString(R.string.dark_mode_enabled), Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, getString(R.string.dark_mode_disabled), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Streak levels functionality moved to ProfileFragment
        
        // Set up the version text
        binding.textVersion.text = getString(R.string.version_format, "1.0.0")
    }
    
    private fun showStreakLevelsDialog() {
        // Create dialog with custom view
        val dialogBinding = DialogStreakLevelsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        // Get all streak levels for display
        val streakLevels = listOf(
            getStreakLevel(0),   // Seed
            getStreakLevel(7),   // Sprout
            getStreakLevel(21),  // Bloom
            getStreakLevel(42),  // Flame
            getStreakLevel(91),  // Wave
            getStreakLevel(181), // Sky
            getStreakLevel(366), // Star
            getStreakLevel(731), // Cosmos
            getStreakLevel(1096), // Aurora
            getStreakLevel(1826), // Prism
            getStreakLevel(3651), // Ethereal
            getStreakLevel(7301)  // Infinite
        )
        
        // Set up the RecyclerView
        val adapter = StreakLevelAdapter(streakLevels)
        dialogBinding.recyclerStreakLevels.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerStreakLevels.adapter = adapter
        
        // Add a close button at the bottom
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        // Set dialog width to match parent and height to wrap content
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun getStreakLevel(days: Int): StreakLevel {
        return when {
            days >= 7301 -> StreakLevel(
                level = 12,
                phase = "transcendence",
                name = "Infinite",
                color = "purple-pink-red",
                icon = "🌈",
                description = "You've achieved legendary status!",
                minDays = 7301,
                maxDays = Int.MAX_VALUE
            )
            days >= 3651 -> StreakLevel(
                level = 11,
                phase = "transcendence",
                name = "Ethereal",
                color = "yellow",
                icon = "✨",
                description = "Your dedication is otherworldly!",
                minDays = 3651,
                maxDays = 7300
            )
            days >= 1826 -> StreakLevel(
                level = 10,
                phase = "transcendence",
                name = "Prism",
                color = "white",
                icon = "💎",
                description = "Reflecting brilliance in all you do!",
                minDays = 1826,
                maxDays = 3650
            )
            days >= 1096 -> StreakLevel(
                level = 9,
                phase = "transcendence",
                name = "Aurora",
                color = "pink",
                icon = "🌌",
                description = "Your productivity illuminates everything!",
                minDays = 1096,
                maxDays = 1825
            )
            days >= 731 -> StreakLevel(
                level = 8,
                phase = "transcendence",
                name = "Cosmos",
                color = "purple",
                icon = "🔮",
                description = "Your habits have reached cosmic significance!",
                minDays = 731,
                maxDays = 1095
            )
            days >= 366 -> StreakLevel(
                level = 7,
                phase = "transformation",
                name = "Star",
                color = "indigo",
                icon = "⭐",
                description = "You're shining brightly!",
                minDays = 366,
                maxDays = 730
            )
            days >= 181 -> StreakLevel(
                level = 6,
                phase = "transformation",
                name = "Sky",
                color = "blue",
                icon = "🌌",
                description = "Your potential is limitless!",
                minDays = 181,
                maxDays = 365
            )
            days >= 91 -> StreakLevel(
                level = 5,
                phase = "transformation",
                name = "Wave",
                color = "cyan",
                icon = "🌊",
                description = "Riding the wave of productivity!",
                minDays = 91,
                maxDays = 180
            )
            days >= 42 -> StreakLevel(
                level = 4,
                phase = "transformation",
                name = "Flame",
                color = "green",
                icon = "🔥",
                description = "Your productivity is on fire!",
                minDays = 42,
                maxDays = 90
            )
            days >= 21 -> StreakLevel(
                level = 3,
                phase = "foundation",
                name = "Bloom",
                color = "yellow",
                icon = "🌼",
                description = "Your habits are blooming!",
                minDays = 21,
                maxDays = 41
            )
            days >= 7 -> StreakLevel(
                level = 2,
                phase = "foundation",
                name = "Sprout",
                color = "orange",
                icon = "🌱",
                description = "Your habits are taking root!",
                minDays = 7,
                maxDays = 20
            )
            else -> StreakLevel(
                level = 1,
                phase = "foundation",
                name = "Seed",
                color = "red",
                icon = "💭",
                description = "Beginning your journey!",
                minDays = 0,
                maxDays = 6
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
