package com.example.budgetwise.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Marks this class as a Room database entity with table name "users"
@Entity(tableName = "users")
data class User(
    // Primary key that Room auto-generates, starting from 1
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // User's login name – stored in 'username' column
    val username: String,
    // Hashed password (SHA-256 + salt) – stored in 'hashedPassword' column
    val hashedPassword: String
)