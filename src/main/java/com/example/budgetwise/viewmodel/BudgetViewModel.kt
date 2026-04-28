package com.example.budgetwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.BudgetGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _budgetGoal = MutableStateFlow<BudgetGoal?>(null)
    val budgetGoal: StateFlow<BudgetGoal?> = _budgetGoal

    fun loadBudgetGoal(userId: Int, month: String) {
        viewModelScope.launch {
            _budgetGoal.value = db.budgetDao().getBudgetGoal(userId, month)
        }
    }

    fun saveBudgetGoal(minGoal: Double, maxGoal: Double, month: String, userId: Int) {
        viewModelScope.launch {
            val goal = BudgetGoal(minGoal = minGoal, maxGoal = maxGoal, month = month, userId = userId)
            db.budgetDao().insertBudgetGoal(goal)
            _budgetGoal.value = goal
        }
    }
}