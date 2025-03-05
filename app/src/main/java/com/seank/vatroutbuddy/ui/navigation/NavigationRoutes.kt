package com.seank.vatroutbuddy.ui.navigation

sealed class NavigationRoutes(val route: String) {
    data object Stockings : NavigationRoutes("stockings")
    data object Notifications : NavigationRoutes("notifications")
    data object StockingDetail : NavigationRoutes("stocking_detail")
} 