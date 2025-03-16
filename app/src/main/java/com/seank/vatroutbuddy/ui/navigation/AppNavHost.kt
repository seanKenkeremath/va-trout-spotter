package com.seank.vatroutbuddy.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.seank.vatroutbuddy.AppConfig
import com.seank.vatroutbuddy.domain.model.StockingInfo
import com.seank.vatroutbuddy.ui.features.detail.StockingDetailScreen
import com.seank.vatroutbuddy.ui.features.notifications.NotificationsScreen
import com.seank.vatroutbuddy.ui.features.stockings.StockingsScreen
import com.seank.vatroutbuddy.ui.features.settings.AboutScreen
import com.seank.vatroutbuddy.ui.features.settings.ContributionsScreen
import com.seank.vatroutbuddy.ui.features.settings.DebugMenuScreen
import com.seank.vatroutbuddy.ui.features.settings.SettingsScreen
import kotlinx.coroutines.delay

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
                NotificationsScreen(collapsibleToolbar = collapsibleNav)
            }

            dialog(
                route = NavigationRoutes.StockingDetail.route,
                dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

                LaunchedEffect(Unit) {
                    dialogWindow?.setWindowAnimations(-1)
                }

                val stocking = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<StockingInfo>("stocking")

                var visible by remember { mutableStateOf(false) }

                val cornerRadius: Dp by animateDpAsState(
                    targetValue = if (visible) 0.dp else 80.dp,
                    animationSpec = tween(
                        durationMillis = AppConfig.MODAL_ANIMATION_DURATION * 2,
                    )
                )

                LaunchedEffect(Unit) {
                    visible = true
                }

                if (stocking != null) {
                    AnimatedVisibility(
                        visible,
                        enter = scaleIn(
                            animationSpec = tween(AppConfig.MODAL_ANIMATION_DURATION)
                        ),
                        exit = scaleOut(
                            animationSpec = tween(AppConfig.MODAL_ANIMATION_DURATION)
                        ),
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(cornerRadius)
                        ) {
                            StockingDetailScreen(
                                stocking = stocking,
                                onBackClick = {
                                    visible = false
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Wait for animation to complete before popping backstack
                        LaunchedEffect(visible) {
                            delay(AppConfig.MODAL_ANIMATION_DURATION.toLong())
                            if (!visible) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }

            composable(NavigationRoutes.Settings.route) {
                SettingsScreen(
                    onDebugMenuClick = { navController.navigate(NavigationRoutes.DebugMenu.route) },
                    onAboutClick = { navController.navigate(NavigationRoutes.About.route) },
                    onContributionsClick = { navController.navigate(NavigationRoutes.Contributions.route) },
                    collapsibleToolbar = collapsibleNav,
                )
            }

            composable(NavigationRoutes.DebugMenu.route) {
                DebugMenuScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavigationRoutes.About.route) {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavigationRoutes.Contributions.route) {
                ContributionsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}