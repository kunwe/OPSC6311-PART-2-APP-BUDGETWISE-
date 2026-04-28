package com.example.budgetwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.DatabaseProvider
import com.example.budgetwise.data.entity.Badge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BadgeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges

    fun loadBadges(userId: Int) {
        viewModelScope.launch {
            _badges.value = db.badgeDao().getBadgesForUser(userId)
        }
    }

    // Award a badge (called from other ViewModels when a goal is reached)
    fun awardBadge(userId: Int, name: String, description: String) {
        viewModelScope.launch {
            val today = java.time.LocalDate.now().toString()
            db.badgeDao().insertBadge(Badge(userId = userId, name = name, description = description, dateEarned = today))
            loadBadges(userId)
        }
    }
}