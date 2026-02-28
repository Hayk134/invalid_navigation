package com.example.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.app.navigation.BottomNavItem
import com.yandex.mapkit.geometry.Point
import androidx.compose.ui.unit.sp

@Composable
fun RoutesScreen(navController: NavHostController) { // Добавь navController
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = "Мои метки и отчеты",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (AppDatabase.userReports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Список пуст", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(AppDatabase.userReports) { report ->
                    UserReportItem(report) {
                        // Устанавливаем цель и переходим на карту
                        AppDatabase.pendingDestination = Point(report.lat, report.lon)
                        navController.navigate(BottomNavItem.Map.route)
                    }
                }
            }
        }
    }
}

@Composable
fun UserReportItem(report: UserReport, onRouteClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (report.type == "Препятствие") Icons.Default.Warning else Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (report.type == "Препятствие") Color.Red else Color.Blue
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(report.title, fontWeight = FontWeight.Bold)
                Text(report.type, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Button(
                    onClick = onRouteClick,
                    modifier = Modifier.padding(top = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Маршрут", fontSize = 12.sp)
                }
            }
        }
    }
}