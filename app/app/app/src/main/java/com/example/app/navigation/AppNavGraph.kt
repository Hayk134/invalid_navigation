package com.example.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.app.presentation.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Login.route,
        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
    ) {
        // Экран логина (уже принимает navController)
        composable(BottomNavItem.Login.route) {
            LoginScreen(navController)
        }

        // Экран регистрации (уже принимает navController)
        composable(BottomNavItem.Register.route) {
            RegisterScreen(navController)
        }

        // Экран карты
        composable(BottomNavItem.Map.route) {
            MapScreen()
        }

        // ЭКРАН ПОИСКА (Теперь передаем navController)
        composable(BottomNavItem.Search.route) {
            SearchScreen(navController)
        }

        // ЭКРАН МАРШРУТОВ (Теперь передаем navController)
        composable(BottomNavItem.Routes.route) {
            RoutesScreen(navController)
        }

        // Экран профиля (уже принимает navController)
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController)
        }
    }
}