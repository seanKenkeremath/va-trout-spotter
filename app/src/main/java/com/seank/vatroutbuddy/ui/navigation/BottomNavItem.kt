package com.seank.vatroutbuddy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Home(
        route = NavigationRoutes.Stockings.route,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    Notifications(
        route = NavigationRoutes.Notifications.route,
        icon = Icons.Default.Notifications,
        label = "Notifications"
    )
} 