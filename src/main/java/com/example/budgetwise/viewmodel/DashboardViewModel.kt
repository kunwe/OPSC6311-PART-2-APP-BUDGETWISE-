package com.example.budgetwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoryTotal(val category: Category, val total: Double)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent

    private val _categoryTotals = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categoryTotals: StateFlow<List<CategoryTotal>> = _categoryTotals

    fun calculateTotals(userId: Int, startDate: String, endDate: String) {
        viewModelScope.launch {
            val expenses = db.expenseDao().getExpensesByPeriod(userId, startDate, endDate)
            val categories = db.categoryDao().getCategoriesByUser(userId)

            val grouped = expenses.groupBy { it.categoryId }
            // Build a CategoryTotal for EVERY category, even if total is 0.0
            val totals = categories.map { cat ->
                val sum = grouped[cat.id]?.sumOf { it.amount } ?: 0.0
                CategoryTotal(cat, sum)
            }
            _categoryTotals.value = totals
            _totalSpent.value = totals.sumOf { it.total }
        }
    }
}