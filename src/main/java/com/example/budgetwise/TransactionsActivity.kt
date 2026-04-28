package com.example.budgetwise

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Badge
import com.example.budgetwise.data.entity.Category
import com.example.budgetwise.data.entity.Expense
import com.example.budgetwise.util.DateUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionsActivity : AppCompatActivity() {

    private lateinit var db: com.example.budgetwise.data.AppDatabase
    private var userId: Int = -1

    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private var startDate: LocalDate = LocalDate.now().withDayOfMonth(1)
    private var endDate: LocalDate = LocalDate.now()

    private lateinit var expensesListView: ListView
    private lateinit var expenseAdapter: ArrayAdapter<String>
    private val expenseList = mutableListOf<Expense>()

    private var categories: List<Category> = emptyList()
    private var photoUri: Uri? = null
    private var pendingPhotoUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingPhotoUri != null) {
            photoUri = pendingPhotoUri
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        photoUri = uri
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            capturePhoto()
        } else {
            Toast.makeText(this, "Camera permission is required for photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

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
        val exportPdfButton = findViewById<Button>(R.id.exportPdfButton)
        expensesListView = findViewById(R.id.expensesListView)
        val addFab = findViewById<FloatingActionButton>(R.id.addExpenseFab)

        updateDateButtonTexts()

        startDateButton.setOnClickListener { showDatePicker(true) }
        endDateButton.setOnClickListener { showDatePicker(false) }
        goButton.setOnClickListener { loadExpenses() }
        exportPdfButton.setOnClickListener { exportToPdf() }

        loadCategories()

        expenseAdapter = ArrayAdapter(this, R.layout.item_expense, R.id.expenseAmountDate, mutableListOf())
        expensesListView.adapter = expenseAdapter

        expensesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < expenseList.size) {
                showExpenseDetailDialog(expenseList[position])
            }
        }

        addFab.setOnClickListener { showAddExpenseDialog() }

        loadExpenses()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesByUser(userId)
        }
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

    private fun loadExpenses() {
        lifecycleScope.launch {
            val startStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val expenses = db.expenseDao().getExpensesByPeriod(userId, startStr, endStr)
            expenseList.clear()
            expenseList.addAll(expenses)

            val displayItems = expenses.map { exp ->
                val cat = categories.find { it.id == exp.categoryId }
                "R${exp.amount} – ${DateUtils.formatDateForDisplay(exp.date)}  ${cat?.name ?: ""}"
            }

            runOnUiThread {
                expenseAdapter.clear()
                expenseAdapter.addAll(displayItems)
                expenseAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun capturePhoto() {
        val cacheDir = File(cacheDir, "receipts")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val photoFile = File(cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
        pendingPhotoUri = FileProvider.getUriForFile(
            this,
            "com.example.budgetwise.fileprovider",
            photoFile
        )
        cameraLauncher.launch(pendingPhotoUri!!)
    }

    private suspend fun savePhotoToDownloads(uri: Uri): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Receipt_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }
        val resolver = contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val imageUri = resolver.insert(collection, contentValues) ?: return null

        resolver.openOutputStream(imageUri)?.use { output ->
            resolver.openInputStream(uri)?.use { input ->
                input.copyTo(output)
            }
        }
        return imageUri.toString()
    }

    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val amountEdit = dialogView.findViewById<EditText>(R.id.amountEditText)
        val dateButton = dialogView.findViewById<Button>(R.id.dateButton)
        val startTimeButton = dialogView.findViewById<Button>(R.id.startTimeButton)
        val endTimeButton = dialogView.findViewById<Button>(R.id.endTimeButton)
        val descriptionEdit = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val photoButton = dialogView.findViewById<Button>(R.id.photoButton)
        val galleryButton = dialogView.findViewById<Button>(R.id.galleryButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        var selectedDate = LocalDate.now()
        var startTime = "08:00"
        var endTime = "09:00"

        dateButton.text = selectedDate.toString()
        startTimeButton.text = startTime
        endTimeButton.text = endTime

        dateButton.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = LocalDate.of(y, m + 1, d)
                dateButton.text = selectedDate.toString()
            }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth).show()
        }

        startTimeButton.setOnClickListener {
            val parts = startTime.split(":")
            TimePickerDialog(this, { _, h, m ->
                startTime = String.format("%02d:%02d", h, m)
                startTimeButton.text = startTime
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        endTimeButton.setOnClickListener {
            val parts = endTime.split(":")
            TimePickerDialog(this, { _, h, m ->
                endTime = String.format("%02d:%02d", h, m)
                endTimeButton.text = endTime
            }, parts[0].toInt(), parts[1].toInt(), true).show()
        }

        val catNames = categories.map { it.name }
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, catNames)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = catAdapter

        photoUri = null
        photoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                capturePhoto()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
        galleryButton.setOnClickListener { galleryLauncher.launch("image/*") }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        saveButton.setOnClickListener {
            val amountText = amountEdit.text.toString()
            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val desc = descriptionEdit.text.toString().trim()
            if (desc.isEmpty()) {
                Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCatIndex = categorySpinner.selectedItemPosition
            val catId = if (selectedCatIndex in categories.indices) categories[selectedCatIndex].id else 0

            lifecycleScope.launch {
                var photoPath: String? = null
                photoUri?.let { uri ->
                    photoPath = savePhotoToDownloads(uri)
                }

                val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val expense = Expense(
                    amount = amount,
                    date = dateStr,
                    startTime = startTime,
                    endTime = endTime,
                    description = desc,
                    categoryId = catId,
                    photoPath = photoPath,
                    userId = userId
                )
                db.expenseDao().insertExpense(expense)

                // Badge check
                val goal = db.budgetDao().getBudgetGoal(userId, DateUtils.currentMonth())
                if (goal != null) {
                    val startStr = DateUtils.currentMonth() + "-01"
                    val endStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val allExpenses = db.expenseDao().getExpensesByPeriod(userId, startStr, endStr)
                    val totalSpent = allExpenses.sumOf { it.amount }
                    if (totalSpent <= goal.maxGoal) {
                        val existingBadges = db.badgeDao().getBadgesForUser(userId)
                        val alreadyEarned = existingBadges.any {
                            it.name == "Budget Keeper" && it.dateEarned.startsWith(DateUtils.currentMonth())
                        }
                        if (!alreadyEarned) {
                            db.badgeDao().insertBadge(
                                Badge(
                                    userId = userId,
                                    name = "Budget Keeper",
                                    description = "Stayed within budget for a full month",
                                    dateEarned = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                )
                            )
                            runOnUiThread {
                                Toast.makeText(
                                    this@TransactionsActivity,
                                    "🏆 Badge earned: Budget Keeper!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                runOnUiThread {
                    Toast.makeText(this@TransactionsActivity, "Expense saved!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadExpenses()
                }
            }
        }
        dialog.show()
    }

    private fun showExpenseDetailDialog(expense: Expense) {
        val catName = categories.find { it.id == expense.categoryId }?.name ?: ""
        val message = "Amount: R${expense.amount}\n" +
                "Date: ${expense.date}\n" +
                "Time: ${expense.startTime} – ${expense.endTime}\n" +
                "Category: $catName\n" +
                "Description: ${expense.description}\n" +
                if (expense.photoPath != null) "Receipt: attached" else "No receipt"
        AlertDialog.Builder(this)
            .setTitle("Expense Detail")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportToPdf() {
        if (expenseList.isEmpty()) {
            Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val pdfDocument = PdfDocument()
                val pageWidth = 595
                val pageHeight = 842
                var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
                var canvas = currentPage.canvas

                val titlePaint = android.graphics.Paint().apply {
                    textSize = 18f; color = android.graphics.Color.BLACK; isFakeBoldText = true
                }
                val headerPaint = android.graphics.Paint().apply {
                    textSize = 12f; color = android.graphics.Color.DKGRAY; isFakeBoldText = true
                }
                val textPaint = android.graphics.Paint().apply {
                    textSize = 11f; color = android.graphics.Color.BLACK
                }
                val linePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.LTGRAY; strokeWidth = 1f
                }

                var y = 50f
                val leftMargin = 20f; val rightMargin = 20f
                val column1X = leftMargin; val column2X = 180f; val column3X = 360f

                canvas.drawText("Expense Report", leftMargin, y, titlePaint)
                y += 25f
                canvas.drawText("Period: $startDate  to  $endDate", leftMargin, y, textPaint)
                y += 30f
                canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, linePaint)
                y += 10f
                canvas.drawText("Amount & Date", column1X, y, headerPaint)
                canvas.drawText("Category & Description", column2X, y, headerPaint)
                canvas.drawText("Receipt", column3X, y, headerPaint)
                y += 15f
                canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, linePaint)
                y += 10f

                for (exp in expenseList) {
                    if (y > pageHeight - 120) {
                        pdfDocument.finishPage(currentPage)
                        currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
                        canvas = currentPage.canvas
                        y = 50f
                        canvas.drawText("Amount & Date", column1X, y, headerPaint)
                        canvas.drawText("Category & Description", column2X, y, headerPaint)
                        canvas.drawText("Receipt", column3X, y, headerPaint)
                        y += 15f
                        canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, linePaint)
                        y += 10f
                    }

                    val cat = categories.find { it.id == exp.categoryId }
                    canvas.drawText("R${exp.amount}  ${exp.date}", column1X, y, textPaint)
                    canvas.drawText("${cat?.name ?: ""}  ${exp.description}", column2X, y, textPaint)

                    if (!exp.photoPath.isNullOrBlank()) {
                        try {
                            val photoUri = Uri.parse(exp.photoPath)
                            val inputStream = contentResolver.openInputStream(photoUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                            if (bitmap != null) {
                                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 80, 60, true)
                                canvas.drawBitmap(scaledBitmap, column3X, y - 15, null)
                            }
                        } catch (e: Exception) {
                            canvas.drawText("(photo unavailable)", column3X, y, textPaint)
                        }
                    } else {
                        canvas.drawText("No receipt", column3X, y, textPaint)
                    }

                    y += 25f
                    canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, linePaint)
                    y += 10f
                }

                pdfDocument.finishPage(currentPage)

                val contentValues = ContentValues().apply {
                    put(MediaStore.Files.FileColumns.DISPLAY_NAME, "Expenses_${System.currentTimeMillis()}.pdf")
                    put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }
                val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    pdfDocument.close()

                    runOnUiThread {
                        Toast.makeText(this@TransactionsActivity, "PDF saved to Downloads!", Toast.LENGTH_LONG).show()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                    }
                } ?: throw Exception("Failed to create file in Downloads")
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@TransactionsActivity, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_transactions
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java).putExtra("USER_ID", userId))
                    finish()
                    true
                }
                R.id.nav_transactions -> true
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