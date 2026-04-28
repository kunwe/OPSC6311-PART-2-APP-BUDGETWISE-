package com.example.budgetwise.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.budgetwise.data.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    // Convenience method that creates a User object and inserts it
    suspend fun addUser(username: String, hashedPassword: String) {
        val user = User(
            username = username,
            hashedPassword = hashedPassword
        )
        insertUser(user)
    }

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>  // for testing / debugging
}