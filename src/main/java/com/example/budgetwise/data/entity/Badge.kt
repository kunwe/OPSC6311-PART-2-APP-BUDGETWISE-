package com.example.budgetwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,          // e.g. "Budget Master"
    val description: String,   // e.g. "Stayed under budget for x months"
    val dateEarned: String     // yyyy-MM-dd
)