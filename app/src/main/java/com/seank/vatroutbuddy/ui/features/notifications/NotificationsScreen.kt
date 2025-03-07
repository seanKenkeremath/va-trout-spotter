package com.seank.vatroutbuddy.ui.features.notifications

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seank.vatroutbuddy.R

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCountyPicker by remember { mutableStateOf(false) }
    var showWaterbodyPicker by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.refreshPermissions()
        }
    }

    // Check permissions when screen is first displayed
    LaunchedEffect(Unit) {
        if (!hasRequestedPermission && !uiState.hasNotificationPermission) {
            hasRequestedPermission = true
            viewModel.requestNotificationPermission(permissionLauncher)
        }
    }

    // Refresh permission status when screen is resumed
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                viewModel.refreshPermissions()
            }
            else -> {}
        }
    }

    if (!uiState.hasNotificationPermission) {
        NotificationsPermissionPrompt(
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            },
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Counties Section
            item {
                SectionHeader(
                    title = stringResource(R.string.notifications_county_section_title),
                    onEditClick = { showCountyPicker = true }
                )
            }

            items(uiState.subscribedCounties.toList().sorted()) { county ->
                SubscriptionItem(
                    text = county,
                    onRemove = { viewModel.toggleCountySubscription(county, false) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Waterbodies Section
            item {
                SectionHeader(
                    title = stringResource(R.string.notifications_waterbody_section_title),
                    onEditClick = { showWaterbodyPicker = true }
                )
            }

            items(uiState.subscribedWaterbodies.toList().sorted()) { waterbody ->
                SubscriptionItem(
                    text = waterbody,
                    onRemove = { viewModel.toggleWaterbodySubscription(waterbody, false) }
                )
            }
        }

        // County Picker Dialog
        if (showCountyPicker) {
            SubscriptionPickerDialog(
                title = stringResource(R.string.notifications_select_counties_title),
                options = uiState.counties.sorted(),
                selectedOptions = uiState.subscribedCounties,
                onDismiss = { showCountyPicker = false },
                onSelectionChanged = { county, selected ->
                    viewModel.toggleCountySubscription(county, selected)
                }
            )
        }

        // Waterbody Picker Dialog
        if (showWaterbodyPicker) {
            SubscriptionPickerDialog(
                title = stringResource(R.string.notifications_select_waterbodies_title),
                options = uiState.waterbodies.sorted(),
                selectedOptions = uiState.subscribedWaterbodies,
                onDismiss = { showWaterbodyPicker = false },
                onSelectionChanged = { waterbody, selected ->
                    viewModel.toggleWaterbodySubscription(waterbody, selected)
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        TextButton(onClick = onEditClick) {
            Text(stringResource(R.string.notifications_edit_button).uppercase())
        }
    }
}

@Composable
private fun SubscriptionItem(
    text: String,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.notifications_remove_button)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionPickerDialog(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onDismiss: () -> Unit,
    onSelectionChanged: (String, Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            LazyColumn {
                items(options) { option ->
                    ListItem(
                        headlineContent = { Text(option) },
                        leadingContent = {
                            Checkbox(
                                checked = option in selectedOptions,
                                onCheckedChange = { checked ->
                                    onSelectionChanged(option, checked)
                                }
                            )
                        },
                        modifier = Modifier.clickable {
                            onSelectionChanged(option, option !in selectedOptions)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notifications_done_button))
            }
        }
    )
}

@Composable
private fun NotificationsPermissionPrompt(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = stringResource(R.string.notifications_permission_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.notifications_permission_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onOpenSettings
            ) {
                Text(stringResource(R.string.notifications_permission_button))
            }
        }
    }
} 