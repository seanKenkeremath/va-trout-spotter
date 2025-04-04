package com.kenkeremath.vatroutspotter.ui.features.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenkeremath.vatroutspotter.R

enum class NotificationTab {
    COUNTIES, WATERBODIES
}

@Composable
fun EditNotificationsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    EditNotificationsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        toggleCountySubscription = viewModel::toggleCountySubscription,
        toggleWaterbodySubscription = viewModel::toggleWaterbodySubscription,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNotificationsContent(
    uiState: NotificationsUiState,
    onBackClick: () -> Unit,
    toggleCountySubscription: (String, Boolean) -> Unit,
    toggleWaterbodySubscription: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NotificationTab.COUNTIES) }
    
    // Create separate search query states for each tab
    var countiesSearchQuery by rememberSaveable { mutableStateOf("") }
    var waterbodiesSearchQuery by rememberSaveable { mutableStateOf("") }
    
    // Create separate scroll states for each tab
    val countiesScrollState = rememberLazyListState()
    val waterbodiesScrollState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                NotificationTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = when (tab) {
                                    NotificationTab.COUNTIES -> stringResource(R.string.notifications_tab_counties)
                                    NotificationTab.WATERBODIES -> stringResource(R.string.notifications_tab_waterbodies)
                                }
                            )
                        }
                    )
                }
            }

            if (uiState is NotificationsUiState.Success) {
                when (selectedTab) {
                    NotificationTab.COUNTIES -> {
                        SubscriptionList(
                            options = uiState.counties.sorted(),
                            selectedOptions = uiState.subscribedCounties,
                            onSelectionChanged = toggleCountySubscription,
                            searchQuery = countiesSearchQuery,
                            searchHint = stringResource(R.string.counties_search_hint),
                            onSearchQueryChange = { countiesSearchQuery = it },
                            scrollState = countiesScrollState
                        )
                    }

                    NotificationTab.WATERBODIES -> {
                        SubscriptionList(
                            options = uiState.waterbodies.sorted(),
                            selectedOptions = uiState.subscribedWaterbodies,
                            onSelectionChanged = toggleWaterbodySubscription,
                            searchQuery = waterbodiesSearchQuery,
                            searchHint = stringResource(R.string.waterbodies_search_hint),
                            onSearchQueryChange = { waterbodiesSearchQuery = it },
                            scrollState = waterbodiesScrollState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionList(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (String, Boolean) -> Unit,
    searchQuery: String,
    searchHint: String,
    onSearchQueryChange: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val filteredOptions = remember(searchQuery, options) {
        if (searchQuery.isBlank()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(searchHint) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true
        )

        if (filteredOptions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.stockings_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                state = scrollState,
            ) {
                items(filteredOptions) { option ->
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
        }
    }
}