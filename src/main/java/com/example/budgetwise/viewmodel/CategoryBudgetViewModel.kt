package com.example.budgetwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.CategoryBudgetLimit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryBudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _categoryLimits = MutableStateFlow<List<CategoryBudgetLimit>>(emptyList())
    val categoryLimits: StateFlow<List<CategoryBudgetLimit>> = _categoryLimits

    fun loadLimits(userId: Int, month: String) {
        viewModelScope.launch {
            _categoryLimits.value = db.categoryBudgetDao().getLimitsForMonth(userId, month)
        }
    }

    fun saveLimit(categoryId: Int, limit: Double, month: String, userId: Int) {
        viewModelScope.launch {
            db.categoryBudgetDao().insertLimit(
                CategoryBudgetLimit(categoryId = categoryId, monthlyLimit = limit, month = month, userId = userId)
            )
            loadLimits(userId, month)
        }
    }

    fun deleteLimit(limit: CategoryBudgetLimit) {
        viewModelScope.launch {
            db.categoryBudgetDao().deleteLimit(limit)
            loadLimits(limit.userId, limit.month)
        }
    }
}