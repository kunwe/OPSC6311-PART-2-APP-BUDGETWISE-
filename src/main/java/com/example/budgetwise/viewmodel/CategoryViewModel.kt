package com.example.budgetwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            val cats = db.categoryDao().getCategoriesByUser(userId)
            if (cats.isEmpty()) {
                insertDefaultCategories(userId)
                _categories.value = db.categoryDao().getCategoriesByUser(userId)
            } else {
                _categories.value = cats
            }
        }
    }

    fun addCategory(name: String, color: String, userId: Int) {
        viewModelScope.launch {
            db.categoryDao().insertCategory(Category(name = name, color = color, userId = userId))
            loadCategories(userId)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            db.categoryDao().updateCategory(category)
            loadCategories(category.userId)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            db.categoryDao().deleteCategory(category)
            loadCategories(category.userId)
        }
    }

    private suspend fun insertDefaultCategories(userId: Int) {
        val defaults = listOf(
            "Groceries", "Transport", "Entertainment", "Rent",
            "Eating Out", "Utilities", "Shopping", "Healthcare", "Other"
        )
        defaults.forEach { name ->
            db.categoryDao().insertCategory(Category(name = name, color = "#757575", userId = userId))
        }
    }
}