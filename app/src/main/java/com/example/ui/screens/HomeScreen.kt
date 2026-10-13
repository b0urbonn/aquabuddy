package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AquaViewModel

@Composable
fun HomeScreen(viewModel: AquaViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val totalMl by viewModel.todayTotal.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    val goal = settings?.dailyGoalMl ?: 2500
    val progress = if (goal > 0) (totalMl.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AquaBuddy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Stay hydrated!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Mascot
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Simple water drop representation
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 50f, topEnd = 50f, bottomStart = 20f, bottomEnd = 20f))
                ) {
                    Box(modifier = Modifier.padding(start = 4.dp, top = 4.dp).size(4.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(50)))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Progress Ring Section
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().weight(1f)) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.outline
            
            Canvas(modifier = Modifier.size(240.dp)) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalMl",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "OF $goal ML",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% Goal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val nextDrinkTime = calculateNextDrinkString(settings, totalMl)
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Next Drink",
                value = nextDrinkTime,
                subtitle = if (nextDrinkTime == "Done") "Goal reached!" else "Stay on track",
                subtitleColor = MaterialTheme.colorScheme.primary
            )
            val streak = settings?.streakCount ?: 0
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Daily Streak",
                value = "$streak Days",
                subtitle = if (streak > 0) "🔥 On fire!" else "Start today!",
                subtitleColor = Color(0xFFF97316) // Orange
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick Add Section
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Log Water",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Custom +",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable { /* Custom */ }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val defaultCup = settings?.cupSize ?: 250
            QuickAddButton(
                modifier = Modifier.weight(1f),
                amount = 200,
                isPrimary = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.addWater(200)
                }
            )
            QuickAddButton(
                modifier = Modifier.weight(1f),
                amount = defaultCup,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.addWater(defaultCup)
                }
            )
            QuickAddButton(
                modifier = Modifier.weight(1f),
                amount = 500,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.addWater(500)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun calculateNextDrinkString(settings: com.example.data.UserSettings?, currentAmount: Int): String {
    if (settings == null) return "--:--"
    if (currentAmount >= settings.dailyGoalMl) return "Done"
    // Simplified placeholder - true logic would check ReminderManager schedule
    return "Auto"
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, subtitleColor: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = subtitleColor)
        }
    }
}

@Composable
fun QuickAddButton(modifier: Modifier = Modifier, amount: Int, isPrimary: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!isPrimary) BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer) else null,
        shadowElevation = if (isPrimary) 4.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$amount", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp,
                color = if (isPrimary) Color.White else MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "ML", 
                fontWeight = FontWeight.Bold, 
                fontSize = 10.sp,
                color = if (isPrimary) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

