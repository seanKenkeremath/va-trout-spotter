package com.seank.vatroutbuddy.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.NavController
import com.seank.vatroutbuddy.AppConfig
import kotlinx.coroutines.delay

/**
 * Dialog wrapper that handles transition animations
 * and the ability to display over top of the bottom nav
 */
@Composable
fun TroutBuddyDialog(
    navController: NavController,
    content: @Composable (onBackClick: () -> Unit) -> Unit
) {
    TroutBuddyDialog({ navController.popBackStack() }, content)
}

@Composable
fun TroutBuddyDialog(
    onNavigateBack: () -> Unit,
    content: @Composable (onBackClick: () -> Unit) -> Unit
) {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    LaunchedEffect(Unit) {
        dialogWindow?.setWindowAnimations(-1)
    }
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
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            content { visible = false }
        }
        // Wait for animation to complete before popping backstack
        LaunchedEffect(visible) {
            delay(AppConfig.MODAL_ANIMATION_DURATION.toLong())
            if (!visible) {
                onNavigateBack()
            }
        }
    }
}