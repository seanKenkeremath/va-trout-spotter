package com.kenkeremath.vatroutspotter.ui.features.stockings

import FilterBottomSheet
import StockingFilters
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.R
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import java.time.format.DateTimeFormatter
import com.kenkeremath.vatroutspotter.ui.components.WavyLoadingIndicator
import com.kenkeremath.vatroutspotter.ui.theme.AppTheme
import java.time.LocalDate

@Composable
fun StockingsScreen(
    onStockingClick: (StockingInfo) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StockingsViewModel = hiltViewModel(),
    collapsibleToolbar: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val refreshingState by viewModel.refreshingState.collectAsState()

    StockingsScreen(
        uiState = uiState,
        pagingState = pagingState,
        refreshingState = refreshingState,
        filters = filters,
        refreshStockings = viewModel::refreshStockings,
        updateFilters = viewModel::updateFilters,
        updateSearchTerm = viewModel::updateSearchTerm,
        onLoadNextPage = viewModel::loadMoreStockings,
        onStockingClick = onStockingClick,
        modifier = modifier,
        collapsibleToolbar = collapsibleToolbar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockingsScreen(
    uiState: HomeUiState,
    refreshingState: RefreshingState,
    pagingState: PagingState,
    filters: StockingFilters,
    refreshStockings: () -> Unit,
    updateFilters: (StockingFilters) -> Unit,
    updateSearchTerm: (String?) -> Unit,
    onStockingClick: (StockingInfo) -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    collapsibleToolbar: Boolean = false,
) {
    var showFilters by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val canScroll by remember { derivedStateOf { listState.canScrollForward || listState.canScrollBackward } }
    val topAppBarState = rememberTopAppBarState()
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState,
        canScroll = { collapsibleToolbar && canScroll }
    )
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.surface
    )
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
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                updateSearchTerm(it)
                            },
                            onClose = {
                                showSearch = false
                                searchQuery = ""
                                updateSearchTerm(null)
                            }
                        )
                    } else {
                        Text(stringResource(R.string.title_stockings))
                    }
                },
                colors = topAppBarColors,
                actions = {
                    if (!showSearch) {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_hint)
                            )
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (filters.activeFilterCount > 0) {
                                Badge(modifier = Modifier.offset(x = (-8).dp, y = 8.dp)) {
                                    Text(filters.activeFilterCount.toString())
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
                },
                scrollBehavior = topBarScrollBehavior,
                windowInsets = WindowInsets(0.dp),
            )
        },
        // We have consumed the bottom padding for the nav bar in the parent scaffold
        contentWindowInsets = WindowInsets(0.dp),
        modifier = modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullToRefresh(
                    isRefreshing = refreshingState == RefreshingState.Refreshing,
                    state = pullRefreshState,
                    onRefresh = refreshStockings
                )
        ) {
            when (val state = uiState) {
                HomeUiState.Uninitialized -> {}
                HomeUiState.LoadingInitialData -> {
                    StockingsInitialLoad(modifier = Modifier.fillMaxSize())
                }

                HomeUiState.Empty -> StockingsEmpty(modifier = Modifier.fillMaxSize())
                is HomeUiState.Success -> {
                    val groupedStockings by remember(state.stockings) {
                        mutableStateOf(state.stockings.groupBy { it.date })
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        // Other padding is applied in child Composables
                        contentPadding = PaddingValues(bottom = 16.dp),
                        content = {
                            for ((date, stockings) in groupedStockings) {
                                stickyHeader {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth()
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(state.messageResId),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(state.bodyResId),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = refreshStockings) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            LaunchedEffect(shouldStartPaginate) {
                if (shouldStartPaginate) {
                    onLoadNextPage()
                }
            }

            // Filter bottom sheet
            if (showFilters) {
                FilterBottomSheet(
                    filters = filters,
                    onFiltersChanged = updateFilters,
                    onDismiss = { showFilters = false }
                )
            }

            var wavesHeight by remember { mutableIntStateOf(0) }

            // Add the water animation overlay when refreshing or on error
            if (uiState is HomeUiState.Error
                || refreshingState is RefreshingState.Refreshing
                || pullRefreshState.distanceFraction > 0f
            ) {
                WavyLoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            wavesHeight = coordinates.size.height
                        }
                        .graphicsLayer {
                            val resolvedTranslationY = if (uiState is HomeUiState.Error) {
                                wavesHeight - (wavesHeight * AppConfig.REFRESH_WAVE_ANIMATION_HEIGHT_FRACTION)
                            } else {
                                wavesHeight -
                                        (wavesHeight * AppConfig.REFRESH_WAVE_ANIMATION_HEIGHT_FRACTION) *
                                        pullRefreshState.distanceFraction
                            }
                            alpha = AppConfig.REFRESH_WAVE_ANIMATION_ALPHA
                            translationY = resolvedTranslationY
                        },
                )
            }
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullRefreshState,
                isRefreshing = refreshingState == RefreshingState.Refreshing,
            )
        }
    }
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
                text = stocking.waterbody,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stocking.county,
                style = MaterialTheme.typography.bodyMedium
            )
            if (stocking.species.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.stockings_species_format,
                        stocking.species.joinToString(", ")
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(query) {
        if (query.isEmpty()) {
            keyboardController?.show()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onClose()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        ) { innerTextField ->
            Box {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                innerTextField()
            }
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun StockingsScreenSuccessPreview() {
    AppTheme {
        StockingsScreen(
            uiState = HomeUiState.Success(
                stockings = listOf(
                    StockingInfo(
                        id = 1L,
                        date = LocalDate.of(2024, 11, 20),
                        county = "County",
                        waterbody = "Lake",
                        category = "A",
                        species = listOf(),
                        isNationalForest = false,
                        isNsf = false,
                        isHeritageDayWater = false,
                        isDelayedHarvest = false,
                    ),
                    StockingInfo(
                        id = 2L,
                        date = LocalDate.of(2024, 11, 20),
                        county = "County2",
                        waterbody = "Lake2",
                        category = "B",
                        species = listOf("Brook Trout"),
                        isNationalForest = false,
                        isNsf = false,
                        isHeritageDayWater = false,
                        isDelayedHarvest = false,
                    ),
                    StockingInfo(
                        id = 3L,
                        date = LocalDate.of(2024, 11, 21),
                        county = "County2",
                        waterbody = "Lake",
                        category = "A",
                        species = listOf("Rainbow Trout"),
                        isNationalForest = false,
                        isNsf = false,
                        isHeritageDayWater = false,
                        isDelayedHarvest = false,
                    )
                )
            ),
            refreshingState = RefreshingState.Idle,
            pagingState = PagingState.Idle,
            filters = StockingFilters(),
            refreshStockings = {},
            updateFilters = { _ -> },
            updateSearchTerm = { _ -> },
            onStockingClick = {},
            onLoadNextPage = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun StockingsScreenErrorPreview() {
    AppTheme {
        StockingsScreen(
            uiState = HomeUiState.Error(
                R.string.generic_error_message,
                R.string.generic_error_body
            ),
            refreshingState = RefreshingState.Idle,
            pagingState = PagingState.Idle,
            filters = StockingFilters(),
            refreshStockings = {},
            updateFilters = { _ -> },
            updateSearchTerm = { _ -> },
            onStockingClick = {},
            onLoadNextPage = {},
        )
    }
}