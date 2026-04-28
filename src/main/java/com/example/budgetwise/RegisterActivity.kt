package com.example.budgetwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.util.HashUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)        // <-- Button
        val loginLink = findViewById<TextView>(R.id.loginLink)                // <-- TextView

        registerButton.setOnClickListener { performRegister() }
        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegister() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirm = confirmPasswordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@RegisterActivity)
            val existing = db.userDao().getUserByUsername(username)
            if (existing != null) {
                runOnUiThread { Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show() }
                return@launch
            }

            val hashed = HashUtils.saltedHash(password)
            db.userDao().addUser(username, hashed)
            runOnUiThread {
                Toast.makeText(this@RegisterActivity, "Registration successful. Please log in.", Toast.LENGTH_SHORT).show()
                // Go back to login
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}