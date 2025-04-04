package com.kenkeremath.vatroutspotter.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.NavController
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.ui.components.WavyLoadingIndicator
import kotlinx.coroutines.delay

/**
 * Dialog wrapper that handles transition animations
 * and the ability to display over top of the bottom nav
 */
@Composable
fun TroutSpotterDialog(
    navController: NavController,
    content: @Composable (onBackClick: () -> Unit) -> Unit
) {
    TroutSpotterDialog({ navController.popBackStack() }, content)
}

@Composable
fun TroutSpotterDialog(
    onNavigateBack: () -> Unit,
    content: @Composable (onBackClick: () -> Unit) -> Unit
) {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    LaunchedEffect(Unit) {
        dialogWindow?.setWindowAnimations(-1)
        dialogWindow?.setDimAmount(0f)
    }
    
    var contentVisible by rememberSaveable { mutableStateOf(false) }
    var isFirstAppearance by rememberSaveable { mutableStateOf(true) }
    
    val cornerRadius: Dp by animateDpAsState(
        targetValue = if (contentVisible) 0.dp else 80.dp,
        animationSpec = tween(
            durationMillis = AppConfig.MODAL_CONTENT_ANIMATION_DURATION_IN * 2,
        )
    )

    BackHandler(enabled = true) {
        contentVisible = false
    }

    Box {
        AnimatedVisibility(
            visible = contentVisible,
            enter = slideInVertically(
                initialOffsetY = { it }, animationSpec = tween(
                    durationMillis = AppConfig.MODAL_LOADING_ANIMATION_DURATION_IN,
                    delayMillis = 0,
                    easing = Ease
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, animationSpec = tween(
                    durationMillis = AppConfig.MODAL_LOADING_ANIMATION_DURATION_OUT,
                    delayMillis = AppConfig.MODAL_ANIMATION_DELAY,
                    easing = EaseOut
                )
            ),
        ) {
            WavyLoadingIndicator(
                modifier = Modifier
                    .alpha(.3f)
                    .fillMaxSize()
                    .padding(top = 32.dp),
                waveLength = 80.dp,
            )
        }
        
        LaunchedEffect(Unit) {
            if (!contentVisible) {
                contentVisible = true
            }
        }

        AnimatedVisibility(
            visible = contentVisible,
            enter = if (isFirstAppearance) {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        delayMillis = AppConfig.MODAL_ANIMATION_DELAY,
                        durationMillis = AppConfig.MODAL_CONTENT_ANIMATION_DURATION_IN
                    )
                )
            } else {
                EnterTransition.None
            },
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = AppConfig.MODAL_CONTENT_ANIMATION_DURATION_OUT)
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(cornerRadius)
            ) {
                content { 
                    contentVisible = false 
                }
            }
        }

        LaunchedEffect(contentVisible) {
            if (contentVisible) {
                // Mark first appearance as complete after animation
                delay(AppConfig.MODAL_CONTENT_ANIMATION_DURATION_IN.toLong())
                isFirstAppearance = false
            } else {
                // Allow navigation to complete before exiting
                delay(AppConfig.MODAL_LOADING_ANIMATION_DURATION_OUT.toLong())
                onNavigateBack()
            }
        }
    }
}