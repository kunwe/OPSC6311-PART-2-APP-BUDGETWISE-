package com.example.budgetwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budget_limits")
data class CategoryBudgetLimit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val monthlyLimit: Double,
    val month: String,
    val userId: Int
)