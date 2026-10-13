package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AquaViewModel

@Composable
fun OnboardingScreen(viewModel: AquaViewModel) {
    var weight by remember { mutableStateOf("70") }
    var activityLevel by remember { mutableStateOf(0) }
    var wakeTime by remember { mutableStateOf("07:00") }
    var sleepTime by remember { mutableStateOf("23:00") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to AquaBuddy 💧", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Let's personalize your hydration.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Activity Level", modifier = Modifier.align(Alignment.Start))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Sedentary", "Active", "Athlete").forEachIndexed { index, title ->
                FilterChip(
                    selected = activityLevel == index,
                    onClick = { activityLevel = index },
                    label = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = wakeTime,
                onValueChange = { wakeTime = it },
                label = { Text("Wake Time (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sleepTime,
                onValueChange = { sleepTime = it },
                label = { Text("Sleep Time (HH:mm)") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                val w = weight.toFloatOrNull() ?: 70f
                var goal = (w * 35).toInt()
                if (activityLevel == 1) goal += 350
                if (activityLevel == 2) goal += 700
                
                viewModel.saveOnboarding(
                    weightKg = w,
                    activityLevel = activityLevel,
                    wakeTime = wakeTime,
                    sleepTime = sleepTime,
                    dailyGoalMl = goal,
                    cupSize = 250
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Calculate & Start")
        }
    }
}
