package com.example.budgetwise.util

import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "budgetwise_prefs"
    private const val KEY_INCOME = "monthly_income"

    fun setIncome(context: Context, income: Double) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_INCOME, income.toFloat()).apply()
    }

    fun getIncome(context: Context): Double {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_INCOME, 5000f).toDouble()  // default 5000
    }
}