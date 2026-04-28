package com.example.budgetwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Name of the category, e.g. "Groceries"
    val name: String,
    // Hex color for UI, default grey
    val color: String = "#757575",
    // Foreign key referencing the user who owns this category
    val userId: Int
)