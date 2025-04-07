package com.kenkeremath.vatroutspotter.ui.navigation

sealed class NavigationRoutes(val route: String) {
    data object Stockings : NavigationRoutes("stockings")
    data object Notifications : NavigationRoutes("notifications")
    data object EditNotifications: NavigationRoutes("edit_notifications")
    data object Settings : NavigationRoutes("settings")
    data object StockingDetail : NavigationRoutes("stocking_detail")
    data object DebugMenu : NavigationRoutes("debug_menu")
    data object About : NavigationRoutes("about")
    object Disclaimer : NavigationRoutes("disclaimer")
}