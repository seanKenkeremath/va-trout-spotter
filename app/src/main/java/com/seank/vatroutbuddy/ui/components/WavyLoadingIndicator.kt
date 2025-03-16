package com.seank.vatroutbuddy.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fadingEdge
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val PI = Math.PI.toFloat()
private fun Int.pi() = this * PI

@Composable
fun WavyLoadingIndicator(
    modifier: Modifier = Modifier,
    crestHeight: Dp = 4.dp,
    waveLength: Dp = 32.dp,
    centerWave: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition()
    val wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val showPoints by remember { mutableStateOf(false) }

    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(Color.Blue, Color.Cyan),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {

        /*
        general form of a sine function
        y = a*sin(b*x - c) + d

        a = amplitude (wave height)
        b = stretch - 2pi / wavelength
        c = phase (x) shift
        d = vertical (y) shift
        */
        val wl = waveLength.toPx()
        val amplitude = crestHeight.toPx()
        val yShift = if (centerWave) size.height / 2 else amplitude
        val stretch = 2.pi() / wl
        val xShift = wave * 2.pi()

        fun sinY(x: Float): Float {
            return amplitude * sin(stretch * x - xShift) + yShift
        }

        val segmentLength = wl / 10f
        val numSegments = (size.width / segmentLength).roundToInt()

        val collectedPoints = mutableListOf<Offset>()

        var pointX = 0f
        val path = Path().apply {
            moveTo(0f, size.height)
            for (segment in 0..numSegments) {
                val pointY = sinY(pointX)

                when (segment) {
                    0 -> lineTo(pointX, pointY)
                    else -> lineTo(pointX, pointY)
                }

                collectedPoints.add(Offset(pointX, pointY))
                pointX = min(pointX + segmentLength, size.width)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
        }

        drawPath(
            path = path,
            brush = gradientBrush,
        )
        if (showPoints) {
            collectedPoints.forEach {
                drawCircle(color = Color.Black, radius = 2f, center = it)
            }
        }
    }
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

@Preview
@Composable
fun WavyLoadingIndicatorCenteredPreview() {
    Surface(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        color = Color.White,
    ) {
        WavyLoadingIndicator(
            centerWave = true
        )
    }
}