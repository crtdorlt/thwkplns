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
import com.theweek.plan.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "🔐 Login Activity started")
        
        userRepository = UserRepository(this)

        // Check if user is already logged in
        lifecycleScope.launch {
            try {
                if (userRepository.isAuthenticated()) {
                    Log.d(TAG, "✅ User already authenticated, navigating to main")
                    navigateToMain()
                } else {
                    Log.d(TAG, "❌ User not authenticated, showing login form")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking authentication", e)
            }
        }

        // Set up click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                login(email, password)
            }
        }

        // Forgot password click
        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Sign up link click
        binding.signupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Please enter a valid email address"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    private fun login(email: String, password: String) {
        Log.d(TAG, "🔐 Attempting login for: $email")
        
        // Show loading state
        setLoading(true)

        lifecycleScope.launch {
            try {
                userRepository.signIn(email, password).fold(
                    onSuccess = { userId ->
                        Log.d(TAG, "✅ Login successful for user: $userId")
                        setLoading(false)
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome back!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToMain()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "❌ Login failed", exception)
                        setLoading(false)
                        val errorMessage = when {
                            exception.message?.contains("Invalid login credentials") == true -> 
                                "Invalid email or password. Please try again."
                            exception.message?.contains("Email not confirmed") == true -> 
                                "Please check your email and confirm your account first."
                            exception.message?.contains("Too many requests") == true -> 
                                "Too many login attempts. Please try again later."
                            exception.message?.contains("User not found") == true -> 
                                "No account found with this email. Please sign up first."
                            else -> "Login failed: ${exception.message}"
                        }
                        Toast.makeText(
                            this@LoginActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Network error during login", e)
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "Network error. Please check your connection and try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun setLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
        binding.passwordInput.isEnabled = !isLoading
        binding.forgotPassword.isEnabled = !isLoading
        binding.signupLink.isEnabled = !isLoading
        
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.text = ""
        } else {
            binding.progressBar.visibility = View.GONE
            binding.loginButton.text = "Login"
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