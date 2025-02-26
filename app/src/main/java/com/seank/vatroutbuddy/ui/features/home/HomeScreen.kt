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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seank.vatroutbuddy.domain.model.StockingInfo
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is HomeUiState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items((uiState as HomeUiState.Success).stockings) { stocking ->
                    StockingItem(stocking)
                }
            }
        }
        is HomeUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (uiState as HomeUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        HomeUiState.Empty -> {
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