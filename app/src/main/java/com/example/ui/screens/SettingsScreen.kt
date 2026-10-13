package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AquaViewModel

@Composable
fun SettingsScreen(viewModel: AquaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    
    if (settings == null) return
    val s = settings!!

    var wakeTime by remember { mutableStateOf(s.wakeTime) }
    var sleepTime by remember { mutableStateOf(s.sleepTime) }
    var goal by remember { mutableStateOf(s.dailyGoalMl.toString()) }
    var cup by remember { mutableStateOf(s.cupSize.toString()) }
    var skipMeals by remember { mutableStateOf(s.skipMealTimes) }
    var notifications by remember { mutableStateOf(s.notificationsEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("Daily Goal (ml)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = cup,
            onValueChange = { cup = it },
            label = { Text("Default Cup Size (ml)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = wakeTime,
                onValueChange = { wakeTime = it },
                label = { Text("Wake (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sleepTime,
                onValueChange = { sleepTime = it },
                label = { Text("Sleep (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Notifications")
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Skip Meal Times (loose schedule)")
            Switch(checked = skipMeals, onCheckedChange = { skipMeals = it })
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                val g = goal.toIntOrNull() ?: 2500
                val c = cup.toIntOrNull() ?: 250
                viewModel.updateGoal(g)
                viewModel.updateSettings(wakeTime, sleepTime, c, skipMeals, notifications)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Save Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
