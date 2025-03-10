package com.seank.vatroutbuddy.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.seank.vatroutbuddy.AppConfig
import com.seank.vatroutbuddy.R
import com.seank.vatroutbuddy.domain.model.StockingInfo
import com.seank.vatroutbuddy.ui.features.detail.StockingDetailScreen
import com.seank.vatroutbuddy.ui.features.notifications.NotificationsScreen
import com.seank.vatroutbuddy.ui.features.stockings.HomeScreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var currentMainRoute by rememberSaveable {
        mutableStateOf(NavigationRoutes.Stockings.route)
    }

    var appBarActions by remember { mutableStateOf<List<@Composable () -> Unit>>(emptyList()) }

    val currentRoute = navBackStackEntry?.destination?.route
    if (currentRoute != null && BottomNavItem.entries.find { it.route == currentRoute } != null) {
        currentMainRoute = currentRoute
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleResId = when (currentMainRoute) {
                        NavigationRoutes.Stockings.route -> R.string.title_stockings
                        NavigationRoutes.Notifications.route -> R.string.title_notifications
                        else -> R.string.app_name
                    }
                    Text(stringResource(titleResId))
                },
                actions = { appBarActions.forEach { it() } }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.Stockings.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationRoutes.Stockings.route) {
                HomeScreen(
                    onUpdateAppBar = { actions ->
                        appBarActions = actions
                    },
                    onStockingClick = { stocking ->
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "stocking",
                            stocking
                        )
                        navController.navigate(NavigationRoutes.StockingDetail.route)
                    }
                )
            }

            composable(NavigationRoutes.Notifications.route) {
                NotificationsScreen(
                    onUpdateAppBar = { actions ->
                        appBarActions = actions
                    }
                )
            }

            dialog(
                route = NavigationRoutes.StockingDetail.route,
                dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

                LaunchedEffect(Unit) {
                    dialogWindow.let { window ->
                        window?.setWindowAnimations(-1)
                    }
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
        }
    }
}