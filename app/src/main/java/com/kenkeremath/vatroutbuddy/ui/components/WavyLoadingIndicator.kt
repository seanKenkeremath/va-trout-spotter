package com.kenkeremath.vatroutbuddy.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.waves.WavyBox
import com.kenkeremath.uselessui.waves.WavyBoxSpec
import com.kenkeremath.uselessui.waves.WavyBoxStyle
import com.kenkeremath.vatroutbuddy.ui.theme.AppTheme
import com.kenkeremath.vatroutbuddy.ui.theme.waveEndDark
import com.kenkeremath.vatroutbuddy.ui.theme.waveEndLight
import com.kenkeremath.vatroutbuddy.ui.theme.waveStartDark
import com.kenkeremath.vatroutbuddy.ui.theme.waveStartLight

@Composable
fun WavyLoadingIndicator(
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 80.dp,
) {


    val gradientStartColor = if (isSystemInDarkTheme()) waveStartDark else waveStartLight
    val gradientEndColor = if (isSystemInDarkTheme()) waveEndDark else waveEndLight

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(gradientEndColor, gradientStartColor),
            start = Offset(0f, Float.POSITIVE_INFINITY),
            end = Offset(0f, 0f)
        )
    }
    val wavyBoxSpec = remember(crestHeight, waveLength) {
        WavyBoxSpec(
            crestHeight = crestHeight,
            waveLength = waveLength,
            topWavy = true,
            bottomWavy = false,
            leftWavy = false,
            rightWavy = false,
        )
    }
    val style = remember {
        WavyBoxStyle.FilledWithBrush(
            brush = gradientBrush,
            strokeWidth = 0.dp,
            strokeColor = Color.Transparent,
        )
    }
    WavyBox(
        modifier = modifier,
        spec = wavyBoxSpec,
        style = style,
    )
}

@PreviewLightDark
@Composable
fun WavyLoadingIndicatorPreview() {
    AppTheme {
        Surface(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
        ) {
            WavyLoadingIndicator()
        }
    }
}

@PreviewLightDark
@Composable
fun WavyLoadingIndicatorTallPreview() {
    AppTheme {
        Surface(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
        ) {
            WavyLoadingIndicator(
                crestHeight = 20.dp
            )
        }
    }
}