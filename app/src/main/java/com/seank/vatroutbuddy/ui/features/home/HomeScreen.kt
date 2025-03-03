package com.seank.vatroutbuddy.ui.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        items(items = state.stockings, key = {
                            "${it.date}, ${it.waterbody}, ${it.date}"
                        }) { stocking ->
                            StockingItem(stocking)
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
                        viewModel.loadMoreCachedStockings()
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
private fun StockingItem(stocking: StockingInfo) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stocking.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${stocking.waterbody}, ${stocking.county}",
            style = MaterialTheme.typography.bodyLarge
        )
        if (stocking.isNationalForest) {
            Text(
                text = "National Forest Water",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            text = "Category: ${stocking.category}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Species: ${stocking.species.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 