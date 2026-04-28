package com.example.budgetwise

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.BudgetGoal
import com.example.budgetwise.data.entity.Category
import com.example.budgetwise.data.entity.CategoryBudgetLimit
import com.example.budgetwise.util.DateUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class BudgetsActivity : AppCompatActivity() {

    private lateinit var db: com.example.budgetwise.data.AppDatabase
    private var userId: Int = -1
    private val currentMonth = DateUtils.currentMonth()

    private lateinit var minGoalEditText: TextInputEditText
    private lateinit var maxGoalEditText: TextInputEditText
    private lateinit var categoriesListView: ListView

    private var categories = listOf<Category>()
    private var categoryLimits = listOf<CategoryBudgetLimit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budgets)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = DatabaseProvider.getDatabase(this)

        minGoalEditText = findViewById(R.id.minGoalEditText)
        maxGoalEditText = findViewById(R.id.maxGoalEditText)
        val saveOverallButton = findViewById<Button>(R.id.saveOverallButton)
        categoriesListView = findViewById(R.id.categoryLimitsListView)
        val fillEnvelopesButton = findViewById<Button>(R.id.fillEnvelopesButton)

        loadOverallBudget()
        loadCategoriesAndLimits()

        saveOverallButton.setOnClickListener { saveOverallBudget() }
        fillEnvelopesButton.setOnClickListener { fillEnvelopesEqually() }

        setupBottomNavigation()
    }

    private fun loadOverallBudget() {
        lifecycleScope.launch {
            val goal = db.budgetDao().getBudgetGoal(userId, currentMonth)
            runOnUiThread {
                minGoalEditText.setText(goal?.minGoal?.toString() ?: "0")
                maxGoalEditText.setText(goal?.maxGoal?.toString() ?: "0")
            }
        }
    }

    private fun loadCategoriesAndLimits() {
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesByUser(userId)
            categoryLimits = db.categoryBudgetDao().getLimitsForMonth(userId, currentMonth)

            runOnUiThread {
                val adapter = CategoryLimitAdapter()
                categoriesListView.adapter = adapter
            }
        }
    }

    private fun saveOverallBudget() {
        val min = minGoalEditText.text.toString().toDoubleOrNull() ?: 0.0
        val max = maxGoalEditText.text.toString().toDoubleOrNull() ?: 0.0
        lifecycleScope.launch {
            db.budgetDao().insertBudgetGoal(
                BudgetGoal(minGoal = min, maxGoal = max, month = currentMonth, userId = userId)
            )
            runOnUiThread { Toast.makeText(this@BudgetsActivity, "Overall budget saved", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun fillEnvelopesEqually() {
        val maxOverall = maxGoalEditText.text.toString().toDoubleOrNull() ?: 0.0
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
            return
        }
        val perCategory = maxOverall / categories.size
        lifecycleScope.launch {
            categories.forEach { cat ->
                db.categoryBudgetDao().insertLimit(
                    CategoryBudgetLimit(categoryId = cat.id, monthlyLimit = perCategory, month = currentMonth, userId = userId)
                )
            }
            loadCategoriesAndLimits()
            runOnUiThread {
                Toast.makeText(this@BudgetsActivity, "Envelopes filled equally: R${"%.2f".format(perCategory)} each", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class CategoryLimitAdapter : BaseAdapter() {
        override fun getCount(): Int = categories.size
        override fun getItem(position: Int): Any = categories[position]
        override fun getItemId(position: Int): Long = categories[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@BudgetsActivity)
                .inflate(R.layout.item_category_limit, parent, false)

            val cat = categories[position]
            val existingLimit = categoryLimits.find { it.categoryId == cat.id }

            val categoryNameText = view.findViewById<TextView>(R.id.categoryNameText)
            val limitEditText = view.findViewById<TextInputEditText>(R.id.limitEditText)
            val saveButton = view.findViewById<Button>(R.id.saveLimitButton)

            categoryNameText.text = cat.name
            limitEditText.setText(existingLimit?.monthlyLimit?.toString() ?: "")

            saveButton.setOnClickListener {
                val newLimit = limitEditText.text.toString().toDoubleOrNull()
                if (newLimit != null) {
                    lifecycleScope.launch {
                        db.categoryBudgetDao().insertLimit(
                            CategoryBudgetLimit(categoryId = cat.id, monthlyLimit = newLimit, month = currentMonth, userId = userId)
                        )
                        categoryLimits = db.categoryBudgetDao().getLimitsForMonth(userId, currentMonth)
                        runOnUiThread {
                            Toast.makeText(this@BudgetsActivity, "Limit for ${cat.name} updated", Toast.LENGTH_SHORT).show()
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            return view
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_budgets
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_budgets -> true
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