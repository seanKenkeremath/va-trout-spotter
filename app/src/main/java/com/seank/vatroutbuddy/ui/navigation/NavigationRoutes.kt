package com.seank.vatroutbuddy.ui.navigation

sealed class NavigationRoutes(val route: String) {
    data object Home : NavigationRoutes("home")
    data object Locations : NavigationRoutes("locations")
    data object Notifications : NavigationRoutes("notifications")
} 