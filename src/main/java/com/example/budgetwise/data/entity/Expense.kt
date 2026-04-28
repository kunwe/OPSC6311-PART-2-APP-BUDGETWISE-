package com.example.budgetwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Amount spent
    val amount: Double,
    // Date in "yyyy-MM-dd" format
    val date: String,
    // Start time in "HH:mm" format
    val startTime: String,
    // End time in "HH:mm" format
    val endTime: String,
    // Short description of the expense
    val description: String,
    // Foreign key to the Category table
    val categoryId: Int,
    // File path to the photo of the receipt (nullable)
    val photoPath: String? = null,
    // Foreign key to the User who created this expense
    val userId: Int
)