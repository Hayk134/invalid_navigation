package com.example.app.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.app.navigation.BottomNavItem
import com.yandex.mapkit.geometry.Point

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Все") }
    val types = listOf("Все", "Еда", "Парки", "Культура", "Магазины", "Развлечения")

    val filteredPlaces = remember(searchQuery, selectedType) {
        globalPlaces.filter {
            val matchSearch = it.name.contains(searchQuery, ignoreCase = true)
            val matchType = selectedType == "Все" || it.type == selectedType
            matchSearch && matchType
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Поиск мест", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), placeholder = { Text("Название...") }, leadingIcon = { Icon(Icons.Default.Search, null) })

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { type ->
                FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type) })
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredPlaces) { place ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(place.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(place.description)
                        Button(onClick = {
                            AppDatabase.pendingDestination = Point(place.lat, place.lon)
                            navController.navigate(BottomNavItem.Map.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Default.LocationOn, null); Text("Маршрут")
                        }
                    }
                }
            }
        }
    }
}