package com.theweek.plan.ui.profile

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.theweek.plan.BuildConfig
import com.theweek.plan.R
import com.theweek.plan.data.repository.TaskSyncRepository
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.databinding.DialogStreakLevelsBinding
import com.theweek.plan.databinding.FragmentProfileBinding
import com.theweek.plan.model.UserProfile
import com.theweek.plan.ui.auth.LoginActivity
import com.theweek.plan.ui.settings.StreakLevelAdapter
import com.theweek.plan.ui.statistics.StreakLevel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userRepository: UserRepository
    private lateinit var taskSyncRepository: TaskSyncRepository
    
    private var userProfile: UserProfile? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userRepository = UserRepository(requireContext())
        taskSyncRepository = TaskSyncRepository(requireContext())
        
        // Load user profile
        loadUserProfile()
        
        // Set up click listeners
        setupClickListeners()
        
        // Set app version
        binding.appVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }
    
    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                userRepository.getUserProfile().fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            userProfile = profile
                            updateUI(profile)
                        } else {
                            // User is not authenticated or profile not found
                            navigateToLogin()
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to load profile: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // If profile loading fails, might need to re-authenticate
                        navigateToLogin()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                navigateToLogin()
            }
        }
    }
    
    private fun updateUI(profile: UserProfile) {
        // Set user info
        binding.displayName.text = if (profile.displayName.isNotEmpty()) {
            profile.displayName
        } else {
            // Extract name from email if display name is empty
            profile.email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        binding.email.text = profile.email
        
        // Set dark mode switch from user profile (now using real data)
        binding.darkModeSwitch.isChecked = profile.prefersDarkMode
        
        // Set week start day
        val weekDays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        binding.weekStartValue.text = weekDays[profile.weekStartsOn]
        
        // Set reminder time
        binding.reminderTimeValue.text = if (profile.reminderTime != null) {
            // Format the time for display
            try {
                val timeParts = profile.reminderTime.split(":")
                if (timeParts.size == 2) {
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                    calendar.set(java.util.Calendar.MINUTE, minute)
                    val displayFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                    displayFormat.format(calendar.time)
                } else {
                    profile.reminderTime
                }
            } catch (e: Exception) {
                profile.reminderTime
            }
        } else {
            "Not set"
        }
        
        // Set last sync time
        updateLastSyncTime(profile.lastSyncTimestamp)
    }
    
    private fun updateLastSyncTime(timestamp: Long) {
        if (timestamp > 0) {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
            binding.lastSyncValue.text = "Last sync: ${format.format(date)}"
        } else {
            binding.lastSyncValue.text = "Never synced"
        }
    }
    
    private fun updateUI(profile: UserProfile) {
        // Set user info
        binding.displayName.text = profile.displayName.ifEmpty { "User" }
        binding.email.text = profile.email
        
        // Set dark mode switch from shared preferences (not from user profile)
        val sharedPrefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        binding.darkModeSwitch.isChecked = sharedPrefs.getBoolean("dark_mode", false)
        
        // Set week start day
        val weekDays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        binding.weekStartValue.text = weekDays[profile.weekStartsOn]
        
        // Set reminder time
        binding.reminderTimeValue.text = profile.reminderTime ?: "Not set"
        
        // Set last sync time
        updateLastSyncTime(profile.lastSyncTimestamp)
    }
    
    private fun updateLastSyncTime(timestamp: Long) {
        if (timestamp > 0) {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.lastSyncValue.text = "Last sync: ${format.format(date)}"
        } else {
            binding.lastSyncValue.text = "Never synced"
        }
    }
    
    private fun setupClickListeners() {
        // Edit profile button
        binding.editProfileButton.setOnClickListener {
            // TODO: Implement edit profile dialog
            Toast.makeText(requireContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Dark mode switch - using the same implementation as in SettingsActivity
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Update user profile with dark mode preference
            userProfile?.let { profile ->
                val updatedProfile = profile.copy(
                    prefersDarkMode = isChecked,
                    updatedAt = System.currentTimeMillis()
                )
                updateUserProfile(updatedProfile)
            }
            
            // Also save to shared preferences for immediate theme change
            val sharedPrefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()
            
            // Apply the theme change
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(requireContext(), getString(R.string.dark_mode_enabled), Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(requireContext(), getString(R.string.dark_mode_disabled), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Week start setting
        binding.weekStartSetting.setOnClickListener {
            val weekDays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            val currentSelection = userProfile?.weekStartsOn ?: 1
            
            AlertDialog.Builder(requireContext())
                .setTitle("Week Starts On")
                .setSingleChoiceItems(weekDays, currentSelection) { dialog, which ->
                    userProfile?.let {
                        val updatedProfile = it.copy(
                            weekStartsOn = which,
                            updatedAt = System.currentTimeMillis()
                        )
                        updateUserProfile(updatedProfile)
                        binding.weekStartValue.text = weekDays[which]
                    }
                    dialog.dismiss()
                }
                .show()
        }
        
        // Reminder time setting
        binding.reminderTimeSetting.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            // Parse current reminder time if set
            userProfile?.reminderTime?.let {
                val timeParts = it.split(":")
                if (timeParts.size == 2) {
                    calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    calendar.set(Calendar.MINUTE, timeParts[1].toInt())
                }
            }
            
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val reminderTime = String.format("%02d:%02d", hourOfDay, minute)
                    userProfile?.let {
                        val updatedProfile = it.copy(
                            reminderTime = reminderTime,
                            updatedAt = System.currentTimeMillis()
                        )
                        updateUserProfile(updatedProfile)
                        
                        // Format for display
                        val displayFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        binding.reminderTimeValue.text = displayFormat.format(calendar.time)
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
        
        // Streak Levels setting
        binding.streakLevelsSetting.setOnClickListener {
            showStreakLevelsDialog()
        }
        
        // View All Streak Levels
        binding.viewAllStreakLevels.setOnClickListener {
            showStreakLevelsDialog()
        }
        
        // Sync now button
        binding.syncNowButton.setOnClickListener {
            syncData()
        }
        
        // Change password
        binding.changePassword.setOnClickListener {
            // TODO: Implement change password functionality
            Toast.makeText(requireContext(), "Change password coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Logout button
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    logout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun updateUserProfile(updatedProfile: UserProfile) {
        lifecycleScope.launch {
            userRepository.updateUserProfile(updatedProfile).fold(
                onSuccess = {
                    userProfile = updatedProfile
                },
                onFailure = { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to update profile: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
    
    private fun syncData() {
        // Show syncing indicator
        binding.syncNowButton.isEnabled = false
        binding.lastSyncValue.text = "Syncing..."
        
        lifecycleScope.launch {
            try {
                // Perform actual sync with Supabase
                val taskViewModel = ViewModelProvider(requireActivity())[com.theweek.plan.ui.tasks.TaskViewModel::class.java]
                val syncSuccess = taskViewModel.performFullSync()
                
                if (syncSuccess) {
                    Toast.makeText(requireContext(), "Sync completed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Sync completed with some issues", Toast.LENGTH_SHORT).show()
                }
                
                // Update the profile with new sync timestamp
                userProfile?.let {
                    val updatedProfile = it.copy(lastSyncTimestamp = System.currentTimeMillis())
                    updateUserProfile(updatedProfile)
                    updateLastSyncTime(updatedProfile.lastSyncTimestamp)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Sync failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.syncNowButton.isEnabled = true
            }
        }
    }
    
    private fun logout() {
        lifecycleScope.launch {
            userRepository.signOut().fold(
                onSuccess = {
                    navigateToLogin()
                },
                onFailure = { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Logout failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    /**
     * Show a dialog with all streak levels
     */
    private fun showStreakLevelsDialog() {
        // Create dialog with custom view
        val dialogBinding = DialogStreakLevelsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
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
        dialogBinding.recyclerStreakLevels.layoutManager = LinearLayoutManager(requireContext())
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
    
    /**
     * Get streak level information based on number of days
     */
    private fun getStreakLevel(days: Int): StreakLevel {
        return when {
            days >= 7301 -> StreakLevel(
                level = 12,
                phase = "transcendence",
                name = "Infinite",
                color = "rainbow",
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
