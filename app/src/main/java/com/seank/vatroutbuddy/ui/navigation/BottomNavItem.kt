package com.seank.vatroutbuddy.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.seank.vatroutbuddy.R

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int
) {
    Stockings(
        route = NavigationRoutes.Stockings.route,
        icon = Icons.Default.Home,
        labelResId = R.string.title_stockings
    ),
    Notifications(
        route = NavigationRoutes.Notifications.route,
        icon = Icons.Default.Notifications,
        labelResId = R.string.title_notifications
    ),
    Settings(
        route = NavigationRoutes.Settings.route,
        icon = Icons.Default.Settings,
        labelResId = R.string.title_settings
    )
} 