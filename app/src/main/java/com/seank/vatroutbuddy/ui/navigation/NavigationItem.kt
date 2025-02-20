package com.seank.vatroutbuddy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Home(
        route = NavigationRoutes.Home.route,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    Dashboard(
        route = NavigationRoutes.Locations.route,
        icon = Icons.Default.LocationOn,
        label = "Location"
    ),
    Notifications(
        route = NavigationRoutes.Notifications.route,
        icon = Icons.Default.Notifications,
        label = "Notifications"
    );

    companion object {
        fun fromRoute(route: String?): NavigationItem? {
            return entries.firstOrNull { it.route == route }
        }
    }
} 