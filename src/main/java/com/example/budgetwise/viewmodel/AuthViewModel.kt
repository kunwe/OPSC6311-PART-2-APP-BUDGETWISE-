package com.example.budgetwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Category
import com.example.budgetwise.util.HashUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _loggedInUserId = MutableStateFlow<Int?>(null)
    val loggedInUserId: StateFlow<Int?> = _loggedInUserId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun register(username: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _errorMessage.value = "Fields cannot be empty"
                return@launch
            }
            if (password != confirmPassword) {
                _errorMessage.value = "Passwords do not match"
                return@launch
            }
            val existing = db.userDao().getUserByUsername(username)
            if (existing != null) {
                _errorMessage.value = "Username already exists"
                return@launch
            }
            val saltedHash = HashUtils.saltedHash(password)
            db.userDao().addUser(username, saltedHash)
            Log.d("AuthViewModel", "User registered: $username")
            _errorMessage.value = null
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _errorMessage.value = "Please enter username and password"
                return@launch
            }
            val user = db.userDao().getUserByUsername(username)
            if (user == null) {
                _errorMessage.value = "User not found"
                return@launch
            }
            if (!HashUtils.verifyPassword(password, user.hashedPassword)) {
                _errorMessage.value = "Incorrect password"
                return@launch
            }
            _loggedInUserId.value = user.id
            Log.d("AuthViewModel", "User logged in: $username")
            _errorMessage.value = null

            // Insert default categories for this user (once)
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
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun logout() { _loggedInUserId.value = null }
}