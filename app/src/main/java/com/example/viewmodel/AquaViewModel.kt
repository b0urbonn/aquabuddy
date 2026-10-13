package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.UserPreferencesRepository
import com.example.data.UserSettings
import com.example.data.WaterLog
import com.example.data.dataStore
import com.example.reminders.ReminderManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class AquaViewModel(
    application: Application,
    private val db: AppDatabase,
    private val prefs: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val reminderManager = ReminderManager(application)
    private val today = LocalDate.now().toString()

    val userSettings: StateFlow<UserSettings?> = prefs.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val todayLogs: StateFlow<List<WaterLog>> = db.waterLogDao().getLogsForDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val todayTotal: StateFlow<Int> = db.waterLogDao().getTotalForDate(today)
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allLogs: StateFlow<List<WaterLog>> = db.waterLogDao().getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            combine(userSettings.filterNotNull(), todayTotal) { settings, total ->
                Pair(settings, total)
            }.collect { (settings, total) ->
                reminderManager.scheduleReminders(settings, total)
            }
        }
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            db.waterLogDao().insertLog(WaterLog(amountMl = amount))
            updateStreak()
        }
    }
    
    fun removeWaterLog(id: Int) {
        viewModelScope.launch {
            db.waterLogDao().deleteLog(id)
        }
    }

    private suspend fun updateStreak() {
        val settings = userSettings.value ?: return
        val lastDate = settings.lastLoggedDate
        
        var currentStreak = settings.streakCount
        var longest = settings.longestStreak
        
        if (lastDate != today) {
            val yesterday = LocalDate.now().minusDays(1).toString()
            if (lastDate == yesterday) {
                currentStreak++
            } else {
                currentStreak = 1
            }
            
            if (currentStreak > longest) {
                longest = currentStreak
            }
            
            prefs.updateStreak(currentStreak, longest, today)
        }
    }

    fun saveOnboarding(
        weightKg: Float,
        activityLevel: Int,
        wakeTime: String,
        sleepTime: String,
        dailyGoalMl: Int,
        cupSize: Int
    ) {
        viewModelScope.launch {
            prefs.saveOnboardingData(weightKg, activityLevel, wakeTime, sleepTime, dailyGoalMl, cupSize)
        }
    }
    
    fun updateSettings(wake: String, sleep: String, cup: Int, skipMeals: Boolean, notifications: Boolean) {
        viewModelScope.launch {
            prefs.updateSettings(wake, sleep, cup, skipMeals, notifications)
        }
    }

    fun updateGoal(goal: Int) {
        viewModelScope.launch {
            prefs.updateGoal(goal)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AquaViewModel::class.java)) {
                val db = Room.databaseBuilder(app, AppDatabase::class.java, "aqua_db").build()
                val prefs = UserPreferencesRepository(app.dataStore)
                @Suppress("UNCHECKED_CAST")
                return AquaViewModel(app, db, prefs) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
