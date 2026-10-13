package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    
    companion object {
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val ACTIVITY_LEVEL = intPreferencesKey("activity_level") // 0=Sedentary, 1=Active, 2=Athlete
        val WAKE_TIME = stringPreferencesKey("wake_time") // HH:mm format
        val SLEEP_TIME = stringPreferencesKey("sleep_time") // HH:mm format
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val CUP_SIZE = intPreferencesKey("cup_size")
        val SKIP_MEAL_TIMES = booleanPreferencesKey("skip_meal_times")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_LOGGED_DATE = stringPreferencesKey("last_logged_date")
    }

    val userPreferencesFlow: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            isOnboarded = prefs[IS_ONBOARDED] ?: false,
            weightKg = prefs[WEIGHT_KG] ?: 70f,
            activityLevel = prefs[ACTIVITY_LEVEL] ?: 0,
            wakeTime = prefs[WAKE_TIME] ?: "07:00",
            sleepTime = prefs[SLEEP_TIME] ?: "23:00",
            dailyGoalMl = prefs[DAILY_GOAL_ML] ?: 2500,
            cupSize = prefs[CUP_SIZE] ?: 250,
            skipMealTimes = prefs[SKIP_MEAL_TIMES] ?: false,
            notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: true,
            streakCount = prefs[STREAK_COUNT] ?: 0,
            longestStreak = prefs[LONGEST_STREAK] ?: 0,
            lastLoggedDate = prefs[LAST_LOGGED_DATE] ?: ""
        )
    }

    suspend fun saveOnboardingData(
        weightKg: Float, 
        activityLevel: Int, 
        wakeTime: String, 
        sleepTime: String, 
        dailyGoalMl: Int, 
        cupSize: Int
    ) {
        dataStore.edit { prefs ->
            prefs[WEIGHT_KG] = weightKg
            prefs[ACTIVITY_LEVEL] = activityLevel
            prefs[WAKE_TIME] = wakeTime
            prefs[SLEEP_TIME] = sleepTime
            prefs[DAILY_GOAL_ML] = dailyGoalMl
            prefs[CUP_SIZE] = cupSize
            prefs[IS_ONBOARDED] = true
        }
    }
    
    suspend fun updateGoal(dailyGoalMl: Int) {
        dataStore.edit { prefs -> prefs[DAILY_GOAL_ML] = dailyGoalMl }
    }
    
    suspend fun updateSettings(
        wakeTime: String, sleepTime: String, cupSize: Int, skipMeals: Boolean, notifications: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[WAKE_TIME] = wakeTime
            prefs[SLEEP_TIME] = sleepTime
            prefs[CUP_SIZE] = cupSize
            prefs[SKIP_MEAL_TIMES] = skipMeals
            prefs[NOTIFICATIONS_ENABLED] = notifications
        }
    }
    
    suspend fun updateStreak(streak: Int, longest: Int, date: String) {
        dataStore.edit { prefs ->
            prefs[STREAK_COUNT] = streak
            prefs[LONGEST_STREAK] = longest
            prefs[LAST_LOGGED_DATE] = date
        }
    }
}

data class UserSettings(
    val isOnboarded: Boolean,
    val weightKg: Float,
    val activityLevel: Int,
    val wakeTime: String,
    val sleepTime: String,
    val dailyGoalMl: Int,
    val cupSize: Int,
    val skipMealTimes: Boolean,
    val notificationsEnabled: Boolean,
    val streakCount: Int,
    val longestStreak: Int,
    val lastLoggedDate: String
)
