package com.example.app.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.app.navigation.BottomNavItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DisabilityCategory(val id: Int, val title: String, val description: String)

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isImporting by remember { mutableStateOf(false) }

    val categoriesList = listOf(
        DisabilityCategory(1, "На коляске", "Обход лестниц."),
        DisabilityCategory(2, "Слабовидящий", "Звуковая навигация."),
        DisabilityCategory(3, "Слабослышащий", "Визуальные табло."),
        DisabilityCategory(4, "Опорно-двигательный аппарат", "Поручни."),
        DisabilityCategory(5, "Родитель с коляской", "Пандусы.")
    )

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Личный кабинет", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { navController.navigate(BottomNavItem.Login.route) { popUpTo(0) } }, modifier = Modifier.fillMaxWidth()) { Text("Выход") }
        }

        item {
            HorizontalDivider()
            Text("Синхронизация", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Госреестр Ростова-на-Дону", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            isImporting = true
                            scope.launch {
                                delay(3000)
                                val newlyAdded = governmentRegistryData.filter { reg -> globalPlaces.none { it.id == reg.id } }
                                globalPlaces.addAll(newlyAdded)
                                isImporting = false
                                if (newlyAdded.isNotEmpty()) {
                                    sendLocalNotification(context, "База обновлена", "Добавлено ${newlyAdded.size} новых мест!")
                                }
                                Toast.makeText(context, "Синхронизация завершена", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isImporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isImporting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("Обновить данные")
                    }
                }
            }
        }

        items(categoriesList) { category ->
            val isSelected = AppDatabase.selectedCategoryId == category.id
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray),
                onClick = { AppDatabase.selectedCategoryId = category.id }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category.title, fontWeight = FontWeight.Bold)
                        Text(category.description, style = MaterialTheme.typography.bodySmall)
                    }
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}