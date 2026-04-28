package com.example.budgetwise

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetwise.data.DatabaseProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InsightsActivity : AppCompatActivity() {

    private lateinit var db: com.example.budgetwise.data.AppDatabase
    private var userId: Int = -1

    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private var startDate: LocalDate = LocalDate.now().withDayOfMonth(1)
    private var endDate: LocalDate = LocalDate.now()

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = DatabaseProvider.getDatabase(this)

        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        val goButton = findViewById<Button>(R.id.goButton)
        listView = findViewById(R.id.categoryTotalsListView)
        barChart = findViewById(R.id.spendingChart)

        val tabTotalsButton = findViewById<Button>(R.id.tabTotalsButton)
        val tabChartButton = findViewById<Button>(R.id.tabChartButton)

        adapter = ArrayAdapter(this, R.layout.item_category_total, R.id.categoryNameText, mutableListOf())
        listView.adapter = adapter

        updateDateButtonTexts()

        startDateButton.setOnClickListener { showDatePicker(true) }
        endDateButton.setOnClickListener { showDatePicker(false) }
        goButton.setOnClickListener { calculateTotals() }

        tabTotalsButton.setOnClickListener {
            listView.visibility = View.VISIBLE
            barChart.visibility = View.GONE
        }
        tabChartButton.setOnClickListener {
            listView.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            calculateTotals()
        }

        calculateTotals()

        setupBottomNavigation()
    }

    private fun updateDateButtonTexts() {
        startDateButton.text = "Start: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        endDateButton.text = "End: ${endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }

    private fun showDatePicker(isStart: Boolean) {
        val current = if (isStart) startDate else endDate
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val newDate = LocalDate.of(year, month + 1, dayOfMonth)
            if (isStart) startDate = newDate else endDate = newDate
            updateDateButtonTexts()
        }, current.year, current.monthValue - 1, current.dayOfMonth).show()
    }

    private fun calculateTotals() {
        lifecycleScope.launch {
            val startStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val expenses = db.expenseDao().getExpensesByPeriod(userId, startStr, endStr)
            val categories = db.categoryDao().getCategoriesByUser(userId)

            val grouped = expenses.groupBy { it.categoryId }
            val totals = categories.map { cat ->
                val sum = grouped[cat.id]?.sumOf { it.amount } ?: 0.0
                Pair(cat.name, sum.toFloat())
            }

            runOnUiThread {
                val displayItems = totals.map { (name, total) -> "$name  –  R${"%.2f".format(total)}" }
                adapter.clear()
                adapter.addAll(displayItems)
                adapter.notifyDataSetChanged()

                updateChart(totals)
            }
        }
    }

    private fun updateChart(data: List<Pair<String, Float>>) {
        val entries = data.mapIndexed { index, (_, value) -> BarEntry(index.toFloat(), value) }
        val dataSet = BarDataSet(entries, "Spending").apply { color = Color.parseColor("#2E7D32") }
        val barData = BarData(dataSet).apply { barWidth = 0.6f }

        barChart.apply {
            this.data = barData
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(data.map { it.first })
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
            }
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            invalidate()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_insights
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
                R.id.nav_budgets -> {
                    startActivity(Intent(this, BudgetsActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_insights -> true
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