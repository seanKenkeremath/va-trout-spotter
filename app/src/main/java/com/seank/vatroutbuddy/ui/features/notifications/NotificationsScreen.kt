package com.seank.vatroutbuddy.ui.features.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCountyPicker by remember { mutableStateOf(false) }
    var showWaterbodyPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Counties Section
        item {
            SectionHeader(
                title = "County Notifications",
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
                title = "Waterbody Notifications",
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
            title = "Select Counties",
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
            title = "Select Waterbodies",
            options = uiState.waterbodies.sorted(),
            selectedOptions = uiState.subscribedWaterbodies,
            onDismiss = { showWaterbodyPicker = false },
            onSelectionChanged = { waterbody, selected ->
                viewModel.toggleWaterbodySubscription(waterbody, selected)
            }
        )
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
            Text("EDIT")
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
                    contentDescription = "Remove notification"
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
                Text("Done")
            }
        }
    )
} 