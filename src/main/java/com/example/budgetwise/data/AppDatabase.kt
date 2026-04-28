package com.example.budgetwise.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.budgetwise.data.dao.*
import com.example.budgetwise.data.entity.*

@Database(
    entities = [
        User::class,
        Category::class,
        Expense::class,
        BudgetGoal::class,
        CategoryBudgetLimit::class,
        Badge::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun badgeDao(): BadgeDao
}