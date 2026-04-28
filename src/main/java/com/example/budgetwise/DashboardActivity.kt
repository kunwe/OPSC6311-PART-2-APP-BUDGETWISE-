package com.example.budgetwise

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Category
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: com.example.budgetwise.data.AppDatabase
    private var userId: Int = -1
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = DatabaseProvider.getDatabase(this)

        listView = findViewById(R.id.categoriesListView)
        val reloadButton = findViewById<Button>(R.id.reloadButton)

        adapter = ArrayAdapter(this, R.layout.item_category, R.id.categoryName, mutableListOf())
        listView.adapter = adapter

        reloadButton.setOnClickListener { loadCategories() }

        loadCategories()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesByUser(userId)
            if (categories.isEmpty()) {
                val defaults = listOf(
                    "Groceries", "Transport", "Entertainment", "Rent",
                    "Eating Out", "Utilities", "Shopping", "Healthcare", "Other"
                )
                defaults.forEach { name ->
                    db.categoryDao().insertCategory(Category(name = name, color = "#757575", userId = userId))
                }
                val updated = db.categoryDao().getCategoriesByUser(userId)
                runOnUiThread {
                    adapter.clear()
                    adapter.addAll(updated.map { it.name })
                    adapter.notifyDataSetChanged()
                }
            } else {
                runOnUiThread {
                    adapter.clear()
                    adapter.addAll(categories.map { it.name })
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_budgets -> {
                    startActivity(Intent(this, BudgetsActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_insights -> {
                    startActivity(Intent(this, InsightsActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}