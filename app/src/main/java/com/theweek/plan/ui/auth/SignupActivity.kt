package com.theweek.plan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.theweek.plan.MainActivity
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)

        // Set up click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign up button click
        binding.signupButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            if (validateInputs(email, password, confirmPassword)) {
                signUp(email, password)
            }
        }

        // Login link click
        binding.loginLink.setOnClickListener {
            finish() // Go back to login screen
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email address"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun signUp(email: String, password: String) {
        // Show loading state
        setLoading(true)

        lifecycleScope.launch {
            userRepository.signUp(email, password).fold(
                onSuccess = { userId ->
                    // Sign up successful
                    setLoading(false)
                    Toast.makeText(
                        this@SignupActivity,
                        "Account created successfully! Please check your email to verify your account.",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToMain()
                },
                onFailure = { exception ->
                    // Sign up failed
                    setLoading(false)
                    Toast.makeText(
                        this@SignupActivity,
                        "Sign up failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.signupButton.text = ""
            binding.signupButton.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.signupButton.text = "Sign Up"
            binding.signupButton.isEnabled = true
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
