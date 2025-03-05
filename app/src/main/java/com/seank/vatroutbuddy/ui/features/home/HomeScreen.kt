@file:OptIn(ExperimentalFoundationApi::class)

package com.seank.vatroutbuddy.ui.features.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seank.vatroutbuddy.domain.model.StockingInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onStockingClick: (StockingInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()
    val listState = remember { LazyListState() }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            HomeUiState.Loading -> LoadingState()
            HomeUiState.Empty -> EmptyState()
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

        // Trigger pagination when scrolled to the end
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1 }
                .collect { isAtEnd ->
                    if (isAtEnd) {
                        viewModel.loadMoreStockings()
                    }
                }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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