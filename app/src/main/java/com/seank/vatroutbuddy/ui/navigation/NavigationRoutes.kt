package com.seank.vatroutbuddy.ui.navigation

sealed class NavigationRoutes(val route: String) {
    data object Main : NavigationRoutes("main")
    data object Stockings : NavigationRoutes("stockings")
    data object Notifications : NavigationRoutes("notifications")
} 