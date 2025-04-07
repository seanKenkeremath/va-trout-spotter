package com.kenkeremath.vatroutspotter.ui.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kenkeremath.vatroutspotter.BuildConfig
import com.kenkeremath.vatroutspotter.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDebugMenuClick: () -> Unit,
    onAboutClick: () -> Unit,
    onAcknowledgementsClick: () -> Unit,
    onDisclaimerClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapsibleToolbar: Boolean = false
) {
    val topAppBarState = rememberTopAppBarState()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState,
        canScroll = { collapsibleToolbar }
    )
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.surface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                colors = topAppBarColors,
                windowInsets = WindowInsets(0.dp),
                scrollBehavior = topBarScrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        modifier = modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Debug menu (only in debug builds)
            if (BuildConfig.DEBUG) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_debug_menu)) },
                    modifier = Modifier.clickable { onDebugMenuClick() }
                )
                HorizontalDivider()
            }

            // About
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_about)) },
                modifier = Modifier.clickable { onAboutClick() }
            )
            HorizontalDivider()

            // Disclaimer
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_disclaimer)) },
                modifier = Modifier.clickable { onDisclaimerClick() }
            )
            HorizontalDivider()

            // Acknowledgements
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_acknowledgements)) },
                modifier = Modifier.clickable { onAcknowledgementsClick() }
            )
            HorizontalDivider()

            // Build version
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_build_version)) },
                supportingContent = {
                    Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                }
            )
            HorizontalDivider()
        }
    }
}