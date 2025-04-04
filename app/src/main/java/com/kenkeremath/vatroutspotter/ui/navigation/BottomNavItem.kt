package com.kenkeremath.vatroutspotter.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kenkeremath.vatroutspotter.R

enum class BottomNavItem(
    val route: String,
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int
) {
    Stockings(
        route = NavigationRoutes.Stockings.route,
        iconResId = R.drawable.ic_trout,
        labelResId = R.string.title_stockings_short
    ),
    Notifications(
        route = NavigationRoutes.Notifications.route,
        iconResId = R.drawable.ic_notifications,
        labelResId = R.string.title_notifications_short
    ),
    Settings(
        route = NavigationRoutes.Settings.route,
        iconResId = R.drawable.ic_settings,
        labelResId = R.string.title_settings
    )
} 