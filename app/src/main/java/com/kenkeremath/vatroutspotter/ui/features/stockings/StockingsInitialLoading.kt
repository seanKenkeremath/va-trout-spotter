package com.kenkeremath.vatroutspotter.ui.features.stockings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.R
import com.kenkeremath.vatroutspotter.ui.components.WavyLoadingIndicator
import com.kenkeremath.vatroutspotter.ui.theme.AppTheme

@Composable
fun StockingsInitialLoad(modifier: Modifier = Modifier) {
    BoxWithConstraints {
        val parentHeight = constraints.maxHeight.toFloat()
        WavyLoadingIndicator(modifier = Modifier.fillMaxSize().graphicsLayer {
            alpha = AppConfig.REFRESH_WAVE_ANIMATION_ALPHA
            translationY = parentHeight/2
        })
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.stockings_initial_load_message),
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@PreviewLightDark
@Composable
fun StockingsInitialLoadPreview() {
    AppTheme {
        Surface {
            StockingsInitialLoad()
        }
    }
}