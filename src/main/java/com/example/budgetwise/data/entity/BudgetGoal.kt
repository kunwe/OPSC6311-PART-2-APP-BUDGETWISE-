package com.example.budgetwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Minimum monthly spending goal
    val minGoal: Double,
    // Maximum monthly spending goal
    val maxGoal: Double,
    // Month identifier, e.g., "2026-04"
    val month: String,
    // User who set this goal
    val userId: Int
)