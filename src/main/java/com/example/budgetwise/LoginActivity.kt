package com.example.budgetwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Category
import com.example.budgetwise.util.HashUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)      // <-- TextView

        loginButton.setOnClickListener { performLogin() }
        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@LoginActivity)
            val user = db.userDao().getUserByUsername(username)
            if (user == null) {
                runOnUiThread { Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show() }
                return@launch
            }
            if (!HashUtils.verifyPassword(password, user.hashedPassword)) {
                runOnUiThread { Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_SHORT).show() }
                return@launch
            }

            // Insert default categories if needed
            val existingCategories = db.categoryDao().getCategoriesByUser(user.id)
            if (existingCategories.isEmpty()) {
                val defaults = listOf(
                    "Groceries", "Transport", "Entertainment", "Rent",
                    "Eating Out", "Utilities", "Shopping", "Healthcare", "Other"
                )
                defaults.forEach { name ->
                    db.categoryDao().insertCategory(
                        Category(name = name, color = "#757575", userId = user.id)
                    )
                }
            }

            // Navigate to Dashboard
            runOnUiThread {
                val intent = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                    putExtra("USER_ID", user.id)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}