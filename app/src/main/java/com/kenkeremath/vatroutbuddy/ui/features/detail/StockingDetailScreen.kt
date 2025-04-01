package com.kenkeremath.vatroutbuddy.ui.features.detail

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.kenkeremath.vatroutbuddy.R
import com.kenkeremath.vatroutbuddy.domain.model.StockingInfo
import com.kenkeremath.vatroutbuddy.ui.theme.AppTheme
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
                text = stringResource(R.string.waterbody_recent_stockings_format, stocking.waterbody),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (relatedStockings.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.waterbody_recent_stockings_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            itemsIndexed(relatedStockings) { index, relatedStocking ->
                RelatedStockingItem(stocking = relatedStocking)
                if (index != relatedStockings.size -1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
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
            DetailRow(label = stringResource(R.string.waterbody_county_label), value = stocking.county)
            DetailRow(label = stringResource(R.string.waterbody_category_label), value = stocking.category)
            Spacer(modifier = Modifier.height(8.dp))
            WaterTypeTags(stocking = stocking)
        }
    }
}

@Composable
private fun WaterTypeTags(
    stocking: StockingInfo,
    modifier: Modifier = Modifier
) {
    val tags = mutableListOf<String>()
    
    if (stocking.isNationalForest) {
        tags.add(stringResource(R.string.waterbody_is_national_forest_water))
    }
    if (stocking.isDelayedHarvest) {
        tags.add(stringResource(R.string.waterbody_is_delayed_harvest_water))
    }
    if (stocking.isHeritageDayWater) {
        tags.add(stringResource(R.string.waterbody_is_heritage_day_water))
    }
    
    if (tags.isEmpty()) return
    
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEachIndexed { index, tag ->
            WaterTypeTag(text = tag)
            
            // Add divider if not the last item
            if (index < tags.size - 1) {
                androidx.compose.material3.VerticalDivider(
                    modifier = Modifier
                        .height(16.dp)
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterVertically),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun WaterTypeTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
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
       if (stocking.species.isNotEmpty()) {
           Spacer(modifier = Modifier.height(4.dp))
           Text(
               text = stringResource(R.string.stockings_species_format, stocking.species.joinToString(", ")),
               style = MaterialTheme.typography.bodyMedium
           )
        }
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