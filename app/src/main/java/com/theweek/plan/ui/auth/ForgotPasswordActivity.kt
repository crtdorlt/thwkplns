package com.theweek.plan.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.databinding.ActivityForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private val TAG = "ForgotPasswordActivity"
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "🔄 Forgot Password Activity started")
        
        userRepository = UserRepository(this)

        // Set up click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back button click
        binding.backButton.setOnClickListener {
            finish()
        }

        // Reset button click
        binding.resetButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (validateInput(email)) {
                resetPassword(email)
            }
        }
    }

    private fun validateInput(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email address"
            return false
        } else {
            binding.emailLayout.error = null
            return true
        }
    }

    private fun resetPassword(email: String) {
        Log.d(TAG, "🔄 Attempting password reset for: $email")
        
        // Show loading state
        setLoading(true)

        lifecycleScope.launch {
            try {
                userRepository.resetPassword(email).fold(
                    onSuccess = {
                        Log.d(TAG, "✅ Password reset email sent successfully")
                        setLoading(false)
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Password reset link has been sent to your email. Please check your inbox and spam folder.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "❌ Password reset failed", exception)
                        setLoading(false)
                        val errorMessage = when {
                            exception.message?.contains("User not found") == true -> 
                                "No account found with this email address."
                            exception.message?.contains("Email rate limit exceeded") == true -> 
                                "Too many reset requests. Please wait before trying again."
                            exception.message?.contains("Invalid email") == true -> 
                                "Please enter a valid email address."
                            else -> "Failed to send reset link: ${exception.message}"
                        }
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Network error during password reset", e)
                setLoading(false)
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Network error. Please check your connection and try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.resetButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
        binding.backButton.isEnabled = !isLoading
        
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.resetButton.text = ""
        } else {
            binding.progressBar.visibility = View.GONE
            binding.resetButton.text = "Send Reset Link"
        }
    }
}