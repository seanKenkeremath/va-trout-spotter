@file:OptIn(ExperimentalFoundationApi::class)

package com.seank.vatroutbuddy.ui.features.stockings

import FilterBottomSheet
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seank.vatroutbuddy.AppConfig
import com.seank.vatroutbuddy.R
import com.seank.vatroutbuddy.domain.model.StockingInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun StockingsScreen(
    onUpdateAppBar: (List<@Composable () -> Unit>) -> Unit,
    onStockingClick: (StockingInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val availableCounties by viewModel.availableCounties.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val shouldStartPaginate by remember {
        derivedStateOf {
            if (pagingState is PagingState.Idle) {
                val layoutInfo = listState.layoutInfo
                val totalItemsCount = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
                lastVisibleItemIndex >= (totalItemsCount - AppConfig.PRELOAD_PAGE_OFFSET)
            } else {
                false
            }
        }
    }

    LaunchedEffect(Unit) {
        onUpdateAppBar(
            listOf {
                BadgedBox(
                    badge = {
                        if (filters.activeFilterCount > 0) {
                            // TODO: centralize padding/use adaptive dimens
                            // This should be half of padding
                            Badge(modifier = Modifier.offset(x = (-8).dp, y = 8.dp)) {
                                Text(
                                    filters.activeFilterCount.toString()
                                )
                            }
                        }
                    },
                ) {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter_black_24dp),
                            contentDescription = "Filters"
                        )
                    }
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            HomeUiState.Uninitialized -> {}
            HomeUiState.LoadingInitialData -> {
                StockingsInitialLoad()
            }
            HomeUiState.Empty -> StockingsEmpty()
            is HomeUiState.Success -> {
                val groupedStockings by remember(state.stockings) {
                    mutableStateOf(state.stockings.groupBy { it.date })
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        for ((date, stockings) in groupedStockings) {
                            stickyHeader {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()

                                ) {
                                    Text(
                                        text = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            items(items = stockings, key = {
                                "${it.date}, ${it.waterbody}, ${it.date}"
                            }) { stocking ->
                                StockingItem(
                                    stocking = stocking,
                                    onClick = { onStockingClick(stocking) }
                                )
                            }
                        }
                        if (pagingState is PagingState.Loading) {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                )
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        LaunchedEffect(shouldStartPaginate) {
            if (shouldStartPaginate) {
                viewModel.loadMoreStockings()
            }
        }

        // Filter bottom sheet
        if (showFilters) {
            FilterBottomSheet(
                filters = filters,
                availableCounties = availableCounties,
                onFiltersChanged = viewModel::updateFilters,
                onClearFilters = viewModel::clearFilters,
                onDismiss = { showFilters = false }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LastUpdatedText(
    lastUpdated: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val formattedTime = remember(lastUpdated) {
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
        lastUpdated.format(formatter)
    }

    Text(
        text = "Last updated: $formattedTime",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun StockingItem(
    stocking: StockingInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${stocking.waterbody}, ${stocking.county}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Species: ${stocking.species.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 