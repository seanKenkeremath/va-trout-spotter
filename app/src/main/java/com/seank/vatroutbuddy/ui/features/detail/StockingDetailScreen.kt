package com.seank.vatroutbuddy.ui.features.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seank.vatroutbuddy.R
import com.seank.vatroutbuddy.domain.model.StockingInfo
import com.seank.vatroutbuddy.ui.theme.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockingDetailScreen(
    stocking: StockingInfo,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockingDetailViewModel = hiltViewModel(
        creationCallback = { factory: StockingDetailViewModel.Factory ->
            factory.create(stocking)
        }
    )
) {
    val locationName by viewModel.locationName.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locationName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is StockingDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is StockingDetailUiState.Success -> {
                    StockingDetailContent(
                        stocking = state.stocking,
                        relatedStockings = state.relatedStockings
                    )
                }
                is StockingDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockingDetailContent(
    stocking: StockingInfo,
    relatedStockings: List<StockingInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            StockingDetailCard(stocking = stocking)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recent Stockings at ${stocking.waterbody}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (relatedStockings.isEmpty()) {
            item {
                Text(
                    text = "No other recent stockings found for this waterbody.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(relatedStockings) { relatedStocking ->
                RelatedStockingItem(stocking = relatedStocking)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun StockingDetailCard(
    stocking: StockingInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.waterbody_details_header),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            DetailRow(label = stringResource(R.string.waterbody_county_label), value = stocking.county)
            DetailRow(label = stringResource(R.string.waterbody_category_label), value = stocking.category)
            if (stocking.isNationalForest) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.waterbody_is_national_forest_water),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RelatedStockingItem(
    stocking: StockingInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stocking.date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Species: ${stocking.species.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@PreviewLightDark
@Composable
private fun StockingDetailContentPreview() {
    AppTheme {
        Surface {
            StockingDetailContent(
                stocking = StockingInfo(
                    id = 1L,
                    waterbody = "Test Waterbody",
                    county = "Test County",
                    category = "Test Category",
                    date = LocalDate.of(2025, 1, 1),
                    species = listOf("Test Species"),
                    isNationalForest = true,
                    isNsf = true,
                    isDelayedHarvest = true,
                    isHeritageDayWater = true,
                ),
                relatedStockings = List(3) {
                    StockingInfo(
                        id = it.toLong(),
                        waterbody = "Test Waterbody",
                        county = "Test County",
                        category = "Test Category",
                        date = LocalDate.of(2025, 1, 1),
                        species = listOf("Test Species"),
                        isNationalForest = true,
                        isNsf = true,
                        isDelayedHarvest = true,
                        isHeritageDayWater = true,
                    )
                }
            )
        }
    }
}