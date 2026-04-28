package com.example.budgetwise.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    fun currentMonth(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun formatDateForDisplay(date: String): String {
        val localDate = LocalDate.parse(date)
        return localDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}