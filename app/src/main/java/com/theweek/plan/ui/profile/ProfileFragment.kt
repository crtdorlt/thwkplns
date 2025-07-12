package com.theweek.plan.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.theweek.plan.BuildConfig
import com.theweek.plan.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
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
        
        // Set up UI with placeholder data
        setupUI()
        
        // Set up click listeners
        setupClickListeners()
        
        // Set app version
        binding.appVersion.text = "Version ${BuildConfig.VERSION_NAME}"
    }
    
    private fun setupUI() {
        // Set placeholder user info
        binding.displayName.text = "Demo User"
        binding.email.text = "demo@example.com"
        
        // Set dark mode switch from shared preferences
        val sharedPrefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        binding.darkModeSwitch.isChecked = sharedPrefs.getBoolean("dark_mode", false)
        
        // Set placeholder values
        binding.weekStartValue.text = "Monday"
        binding.reminderTimeValue.text = "9:00 AM"
        binding.lastSyncValue.text = "Local storage only"
    }
    
    private fun setupClickListeners() {
        // Edit profile button
        binding.editProfileButton.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Dark mode switch
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save to shared preferences
            val sharedPrefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()
            
            // Apply the theme change
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(requireContext(), "Dark mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(requireContext(), "Dark mode disabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Other settings
        binding.weekStartSetting.setOnClickListener {
            Toast.makeText(requireContext(), "Week start setting coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.reminderTimeSetting.setOnClickListener {
            Toast.makeText(requireContext(), "Reminder time setting coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.streakLevelsSetting.setOnClickListener {
            Toast.makeText(requireContext(), "Streak levels coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.viewAllStreakLevels.setOnClickListener {
            Toast.makeText(requireContext(), "Streak levels coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.syncNowButton.setOnClickListener {
            Toast.makeText(requireContext(), "Sync functionality coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.changePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Change password coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.logoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Logout functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}