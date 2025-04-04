package com.kenkeremath.vatroutbuddy.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kenkeremath.uselessui.waves.WavyBox
import com.kenkeremath.uselessui.waves.WavyBoxSpec
import com.kenkeremath.uselessui.waves.WavyBoxStyle
import fadingEdge

@Composable
fun WavyLoadingIndicator(
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 80.dp,
) {

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(Color.Blue, Color.Cyan),
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


@Preview
@Composable
fun WavyLoadingIndicatorWithFadePreview() {
    val leftRightFade = Brush.horizontalGradient(
        0f to Color.Transparent,
        0.1f to Color.Red,
        0.9f to Color.Red,
        1f to Color.Transparent
    )

    Surface(
        color = Color.White,
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
    ) {
        WavyLoadingIndicator(
            Modifier
                .padding(32.dp)
                .fadingEdge(leftRightFade)
        )
    }
}

@Preview
@Composable
fun WavyLoadingIndicatorPreview() {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        color = Color.White,
    ) {
        WavyLoadingIndicator()
    }
}

@Preview
@Composable
fun WavyLoadingIndicatorTallPreview() {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        color = Color.White,
    ) {
        WavyLoadingIndicator(
            crestHeight = 20.dp
        )
    }
}