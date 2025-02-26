package com.seank.vatroutbuddy.ui.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    HomeScreen(
        uiState = uiState,
        onRefresh = viewModel::refreshStockings,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            HomeUiState.Loading -> LoadingState()
            HomeUiState.Empty -> EmptyState()
            is HomeUiState.Success -> {
                Column {
                    LastUpdatedText(
                        lastUpdated = uiState.lastUpdatedAt,
                        modifier = Modifier.padding(16.dp)
                    )
                    val stockings = remember(uiState.stockings) { uiState.stockings }
                    StockingList(stockings = stockings)
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
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
private fun StockingList(
    stockings: List<StockingInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = stockings,
            key = { stocking -> "${stocking.date}_${stocking.waterbody}" }
        ) { stocking ->
            StockingItem(stocking = stocking)
        }
    }
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