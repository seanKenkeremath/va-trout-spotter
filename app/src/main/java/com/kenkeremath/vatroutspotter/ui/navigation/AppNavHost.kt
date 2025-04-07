package com.kenkeremath.vatroutspotter.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.R
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import com.kenkeremath.vatroutspotter.ui.features.detail.StockingDetailScreen
import com.kenkeremath.vatroutspotter.ui.features.notifications.NotificationsScreen
import com.kenkeremath.vatroutspotter.ui.features.settings.AboutScreen
import com.kenkeremath.vatroutspotter.ui.features.settings.DebugMenuScreen
import com.kenkeremath.vatroutspotter.ui.features.settings.SettingsScreen
import com.kenkeremath.vatroutspotter.ui.features.stockings.StockingsScreen
import com.kenkeremath.vatroutspotter.ui.features.notifications.EditNotificationsScreen
import com.kenkeremath.vatroutspotter.ui.features.settings.DisclaimerScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val collapsibleNav = AppConfig.ALLOW_COLLAPSIBLE_NAV
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.Stockings.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationRoutes.Stockings.route) {
                StockingsScreen(
                    onStockingClick = { stocking ->
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "stocking",
                            stocking
                        )
                        navController.navigate(NavigationRoutes.StockingDetail.route)
                    },
                    collapsibleToolbar = collapsibleNav,
                )
            }

            composable(NavigationRoutes.Notifications.route) {
                NotificationsScreen(onEditNotificationsClicked = {
                    navController.navigate(NavigationRoutes.EditNotifications.route)
                })
            }

            fullscreenDialog(
                route = NavigationRoutes.EditNotifications.route,
                navController = navController,
            ) { onBackClick ->
                EditNotificationsScreen(
                    onBackClick = onBackClick
                )
            }

            fullscreenDialog(
                route = NavigationRoutes.StockingDetail.route,
                navController = navController,
            ) { onBackClick ->
                val stocking = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<StockingInfo>("stocking")
                    ?: throw IllegalArgumentException("No stocking data passed to detail screen")
                StockingDetailScreen(
                    stocking = stocking,
                    onBackClick = onBackClick,
                )
            }

            composable(NavigationRoutes.Settings.route) {
                SettingsScreen(
                    onDebugMenuClick = { navController.navigate(NavigationRoutes.DebugMenu.route) },
                    onAboutClick = { navController.navigate(NavigationRoutes.About.route) },
                    onDisclaimerClick = { navController.navigate(NavigationRoutes.Disclaimer.route) },
                    onAcknowledgementsClick = {
                        val context = navController.context
                        val intent = Intent(context, OssLicensesMenuActivity::class.java)
                        OssLicensesMenuActivity.setActivityTitle(context.getString(R.string.title_acknowledgements))
                        context.startActivity(intent)
                    },
                    collapsibleToolbar = collapsibleNav,
                )
            }

            fullscreenDialog(
                navController = navController,
                route = NavigationRoutes.DebugMenu.route
            ) { onBackClick ->
                DebugMenuScreen(
                    onBackClick = onBackClick
                )
            }

            fullscreenDialog(
                navController = navController,
                route = NavigationRoutes.About.route,
            ) { onBackClick ->
                AboutScreen(
                    onBackClick = onBackClick
                )
            }

            fullscreenDialog(
                navController = navController,
                route = NavigationRoutes.Disclaimer.route,
            ) { onBackClick ->
                DisclaimerScreen(
                    onBackClick = onBackClick
                )
            }
        }
    }
}

private fun NavGraphBuilder.fullscreenDialog(
    route: String,
    navController: NavController,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (onBackClick: () -> Unit) -> Unit
) {
    dialog(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
        content = {
            TroutSpotterDialog(navController) { onBackClick ->
                content(onBackClick)
            }
        },
    )
}