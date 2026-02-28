package com.example.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Map : BottomNavItem("map", "Карта", Icons.Default.Place)
    object Search : BottomNavItem("search", "Поиск", Icons.Default.Search)
    object Routes : BottomNavItem("routes", "Маршруты", Icons.AutoMirrored.Filled.List)
    object Profile : BottomNavItem("profile", "Профиль", Icons.Default.Person)
    object Login : BottomNavItem("login", "Вход", Icons.Default.Person)
    object Register : BottomNavItem("register", "Регистрация", Icons.Default.Person)
}