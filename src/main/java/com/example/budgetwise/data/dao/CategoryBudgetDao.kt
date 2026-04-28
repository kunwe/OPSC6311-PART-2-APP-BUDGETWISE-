package com.example.budgetwise.data.dao

import androidx.room.*
import com.example.budgetwise.data.entity.CategoryBudgetLimit

@Dao
interface CategoryBudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: CategoryBudgetLimit)

    @Query("SELECT * FROM category_budget_limits WHERE userId = :userId AND month = :month")
    suspend fun getLimitsForMonth(userId: Int, month: String): List<CategoryBudgetLimit>

    @Delete
    suspend fun deleteLimit(limit: CategoryBudgetLimit)
}