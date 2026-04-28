package com.example.budgetwise.data.dao

import androidx.room.*
import com.example.budgetwise.data.entity.Badge

@Dao
interface BadgeDao {
    @Insert
    suspend fun insertBadge(badge: Badge)

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY dateEarned DESC")
    suspend fun getBadgesForUser(userId: Int): List<Badge>
}