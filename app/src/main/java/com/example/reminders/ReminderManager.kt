package com.example.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.UserSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminders(settings: UserSettings, currentAmount: Int) {
        if (!settings.notificationsEnabled || !settings.isOnboarded) {
            cancelAllReminders()
            return
        }

        val now = LocalDateTime.now()
        val wake = LocalTime.parse(settings.wakeTime, DateTimeFormatter.ofPattern("HH:mm"))
        val sleep = LocalTime.parse(settings.sleepTime, DateTimeFormatter.ofPattern("HH:mm"))
        
        val startDateTime = LocalDateTime.of(LocalDate.now(), wake)
        var endDateTime = LocalDateTime.of(LocalDate.now(), sleep)
        if (endDateTime.isBefore(startDateTime)) {
            endDateTime = endDateTime.plusDays(1)
        }

        // If it's already past sleep time, don't schedule for today, schedule for tomorrow
        if (now.isAfter(endDateTime)) {
            return
        }

        val remainingGoal = settings.dailyGoalMl - currentAmount
        if (remainingGoal <= 0) {
            cancelAllReminders()
            return
        }

        val numDrinksRemaining = Math.ceil(remainingGoal.toDouble() / settings.cupSize.toDouble()).toInt()
        
        // Calculate remaining minutes in the day's window
        val effectiveStart = if (now.isAfter(startDateTime)) now else startDateTime
        val remainingMinutes = java.time.Duration.between(effectiveStart, endDateTime).toMinutes()
        
        if (numDrinksRemaining <= 0 || remainingMinutes <= 0) return

        // Interval in minutes
        val intervalMinutes = remainingMinutes / numDrinksRemaining
        
        cancelAllReminders()

        for (i in 1..numDrinksRemaining) {
            val reminderTime = effectiveStart.plusMinutes(intervalMinutes * i)
            if (reminderTime.isAfter(endDateTime)) break
            
            // Skip meal times loosely if enabled (e.g., skip between 8-9am, 12-1pm, 6-7pm)
            if (settings.skipMealTimes) {
                val hour = reminderTime.hour
                if (hour == 8 || hour == 12 || hour == 18) {
                    continue // Skip this slot
                }
            }
            
            scheduleAlarm(reminderTime, i, settings.cupSize, currentAmount + (i-1)*settings.cupSize, settings.dailyGoalMl)
        }
    }

    private fun scheduleAlarm(time: LocalDateTime, id: Int, amountToDrink: Int, current: Int, goal: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_WATER_REMINDER"
            putExtra("AMOUNT", amountToDrink)
            putExtra("CURRENT", current)
            putExtra("GOAL", goal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeInMillis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Permission for exact alarm not granted
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                15 * 60 * 1000, // 15 mins window
                pendingIntent
            )
        }
    }

    fun cancelAllReminders() {
        // Broadly cancel a large range of possible alarm IDs
        for (i in 1..20) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.ACTION_WATER_REMINDER"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, i, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
