package com.theweek.plan.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.databinding.ActivityForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // Show loading state
        setLoading(true)

        lifecycleScope.launch {
            userRepository.resetPassword(email).fold(
                onSuccess = {
                    // Password reset email sent successfully
                    setLoading(false)
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Password reset link has been sent to your email",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                },
                onFailure = { exception ->
                    // Password reset failed
                    setLoading(false)
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Failed to send reset link: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.resetButton.text = ""
            binding.resetButton.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.resetButton.text = "Send Reset Link"
            binding.resetButton.isEnabled = true
        }
    }
}
