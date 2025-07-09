package com.theweek.plan.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
// import com.theweek.plan.BuildConfig
import com.theweek.plan.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Load saved settings
        loadSettings()
        
        // Setup dark mode switch
        setupDarkModeSwitch()
        
        // Setup notifications switch
        setupNotificationsSwitch()
        
        // Setup reminder time picker
        setupReminderTimePicker()
        
        // Setup version info
        setupVersionInfo()
    }

    private fun loadSettings() {
        val sharedPrefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        // Load dark mode setting
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDarkMode
        
        // Load notifications setting
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        binding.switchNotifications.isChecked = notificationsEnabled
        
        // Load reminder time
        val reminderHour = sharedPrefs.getInt("reminder_hour", 8)
        val reminderMinute = sharedPrefs.getInt("reminder_minute", 0)
        calendar.set(Calendar.HOUR_OF_DAY, reminderHour)
        calendar.set(Calendar.MINUTE, reminderMinute)
        binding.textSelectedTime.text = timeFormatter.format(calendar.time)
    }

    private fun setupDarkModeSwitch() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save setting
            requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("dark_mode", isChecked)
                .apply()
            
            // Apply dark mode
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupNotificationsSwitch() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save setting
            requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("notifications_enabled", isChecked)
                .apply()
            
            // Enable/disable reminder time selection
            binding.textReminderTime.isEnabled = isChecked
            binding.textSelectedTime.isEnabled = isChecked
        }
    }

    private fun setupReminderTimePicker() {
        binding.textSelectedTime.setOnClickListener {
            if (!binding.switchNotifications.isChecked) return@setOnClickListener
            
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    binding.textSelectedTime.text = timeFormatter.format(calendar.time)
                    
                    // Save setting
                    requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("reminder_hour", hourOfDay)
                        .putInt("reminder_minute", minute)
                        .apply()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        }
    }

    private fun setupVersionInfo() {
        binding.textVersionNumber.text = "1.0.0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
