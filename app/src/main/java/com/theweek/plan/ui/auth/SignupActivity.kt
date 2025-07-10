package com.theweek.plan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.theweek.plan.MainActivity
import com.theweek.plan.data.repository.UserRepository
import com.theweek.plan.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private val TAG = "SignupActivity"
    private lateinit var binding: ActivitySignupBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "📝 Signup Activity started")
        
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
        Log.d(TAG, "📝 Attempting signup for: $email")
        
        // Show loading state
        setLoading(true)

        lifecycleScope.launch {
            try {
                userRepository.signUp(email, password).fold(
                    onSuccess = { userId ->
                        Log.d(TAG, "✅ Signup successful for user: $userId")
                        setLoading(false)
                        Toast.makeText(
                            this@SignupActivity,
                            "Account created successfully! Welcome to The Week Plan!",
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToMain()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "❌ Signup failed", exception)
                        setLoading(false)
                        val errorMessage = when {
                            exception.message?.contains("User already registered") == true -> 
                                "An account with this email already exists. Please try logging in instead."
                            exception.message?.contains("Password should be at least") == true -> 
                                "Password must be at least 6 characters long."
                            exception.message?.contains("Invalid email") == true -> 
                                "Please enter a valid email address."
                            exception.message?.contains("Signup is disabled") == true -> 
                                "Account creation is currently disabled. Please contact support."
                            exception.message?.contains("weak password") == true -> 
                                "Password is too weak. Please use a stronger password."
                            else -> "Sign up failed: ${exception.message}"
                        }
                        Toast.makeText(
                            this@SignupActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Network error during signup", e)
                setLoading(false)
                Toast.makeText(
                    this@SignupActivity,
                    "Network error. Please check your connection and try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.signupButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
        binding.passwordInput.isEnabled = !isLoading
        binding.confirmPasswordInput.isEnabled = !isLoading
        binding.loginLink.isEnabled = !isLoading
        
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.signupButton.text = ""
        } else {
            binding.progressBar.visibility = View.GONE
            binding.signupButton.text = "Sign Up"
        }
    }
    
    private fun navigateToMain() {
        Log.d(TAG, "🏠 Navigating to main activity")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}