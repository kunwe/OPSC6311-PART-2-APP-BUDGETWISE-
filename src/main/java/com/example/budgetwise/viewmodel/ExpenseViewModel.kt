package com.example.budgetwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _selectedExpense = MutableStateFlow<Expense?>(null)
    val selectedExpense: StateFlow<Expense?> = _selectedExpense

    fun addExpense(
        amount: Double, date: String, startTime: String, endTime: String,
        description: String, categoryId: Int, photoPath: String?, userId: Int
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount, date = date, startTime = startTime,
                endTime = endTime, description = description,
                categoryId = categoryId, photoPath = photoPath, userId = userId
            )
            db.expenseDao().insertExpense(expense)
            Log.d("ExpenseViewModel", "Expense added: $expense")
        }
    }

    fun loadExpenses(userId: Int, startDate: String, endDate: String) {
        viewModelScope.launch {
            _expenses.value = db.expenseDao().getExpensesByPeriod(userId, startDate, endDate)
        }
    }

    fun loadExpenseById(expenseId: Int) {
        viewModelScope.launch {
            _selectedExpense.value = db.expenseDao().getExpenseById(expenseId)
        }
    }
}