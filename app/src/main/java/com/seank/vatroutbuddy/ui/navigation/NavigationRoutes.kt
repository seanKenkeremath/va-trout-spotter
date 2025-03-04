package com.seank.vatroutbuddy.ui.navigation

sealed class NavigationRoutes(val route: String) {
    data object Home : NavigationRoutes("home")
    data object Notifications : NavigationRoutes("notifications")
} 