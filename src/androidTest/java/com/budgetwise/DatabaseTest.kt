package com.example.budgetwise

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgetwise.data.AppDatabase
import com.example.budgetwise.data.entity.*
import com.example.budgetwise.util.HashUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // ---------- User tests ----------
    @Test
    fun insertAndRetrieveUser() = runBlocking {
        val username = "testuser"
        val hashed = HashUtils.saltedHash("password123")
        val user = User(username = username, hashedPassword = hashed)
        db.userDao().insertUser(user)

        val retrieved = db.userDao().getUserByUsername(username)
        assertNotNull(retrieved)
        assertEquals(username, retrieved?.username)
        assertTrue(HashUtils.verifyPassword("password123", retrieved!!.hashedPassword))
    }

    @Test
    fun loginWithIncorrectPasswordFails() = runBlocking {
        val user = User(username = "wrong", hashedPassword = HashUtils.saltedHash("correct"))
        db.userDao().insertUser(user)

        val retrieved = db.userDao().getUserByUsername("wrong")
        assertFalse(HashUtils.verifyPassword("wrongpass", retrieved!!.hashedPassword))
    }

    // ---------- Category tests ----------
    @Test
    fun insertAndRetrieveCategories() = runBlocking {
        val userId = 1
        val category = Category(name = "Groceries", color = "#757575", userId = userId)
        db.categoryDao().insertCategory(category)

        val categories = db.categoryDao().getCategoriesByUser(userId)
        assertTrue(categories.isNotEmpty())
        assertEquals("Groceries", categories[0].name)
    }

    @Test
    fun categoriesForDifferentUsersAreSeparated() = runBlocking {
        val user1Id = 1
        val user2Id = 2
        db.categoryDao().insertCategory(Category(name = "User1Cat", userId = user1Id))
        db.categoryDao().insertCategory(Category(name = "User2Cat", userId = user2Id))

        // User 1 sees only their own category
        val user1Cats = db.categoryDao().getCategoriesByUser(user1Id)
        assertEquals(1, user1Cats.size)
        assertEquals("User1Cat", user1Cats[0].name)

        // User 2 sees only their own category
        val user2Cats = db.categoryDao().getCategoriesByUser(user2Id)
        assertEquals(1, user2Cats.size)
        assertEquals("User2Cat", user2Cats[0].name)
    }

    // ---------- Expense tests ----------
    @Test
    fun insertAndRetrieveExpensesByPeriod() = runBlocking {
        val userId = 1
        val categoryId = 1
        // Insert category first
        db.categoryDao().insertCategory(Category(id = categoryId, name = "TestCat", userId = userId))

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val expense = Expense(
            amount = 100.0,
            date = today,
            startTime = "08:00",
            endTime = "09:00",
            description = "Lunch",
            categoryId = categoryId,
            photoPath = null,
            userId = userId
        )
        db.expenseDao().insertExpense(expense)

        val expenses = db.expenseDao().getExpensesByPeriod(userId, today, today)
        assertEquals(1, expenses.size)
        assertEquals(100.0, expenses[0].amount, 0.0)
    }

    // ---------- Budget Goal tests ----------
    @Test
    fun insertAndRetrieveBudgetGoal() = runBlocking {
        val userId = 1
        val month = "2026-05"
        val goal = BudgetGoal(minGoal = 0.0, maxGoal = 5000.0, month = month, userId = userId)
        db.budgetDao().insertBudgetGoal(goal)

        val retrieved = db.budgetDao().getBudgetGoal(userId, month)
        assertNotNull(retrieved)
        assertEquals(5000.0, retrieved!!.maxGoal, 0.0)
    }

    // ---------- Badge tests ----------
    @Test
    fun insertAndRetrieveBadges() = runBlocking {
        val userId = 1
        val badge = Badge(
            userId = userId,
            name = "Budget Keeper",
            description = "Stayed within budget",
            dateEarned = "2026-05-01"
        )
        db.badgeDao().insertBadge(badge)

        val badges = db.badgeDao().getBadgesForUser(userId)
        assertEquals(1, badges.size)
        assertEquals("Budget Keeper", badges[0].name)
    }
}