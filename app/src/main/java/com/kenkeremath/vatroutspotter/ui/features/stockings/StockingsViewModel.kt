package com.kenkeremath.vatroutspotter.ui.features.stockings

import StockingFilters
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenkeremath.vatroutspotter.AppConfig
import com.kenkeremath.vatroutspotter.data.repository.StockingRepository
import com.kenkeremath.vatroutspotter.di.IoDispatcher
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import com.kenkeremath.vatroutspotter.domain.usecase.FetchAndNotifyStockingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.TreeSet
import javax.inject.Inject
import com.kenkeremath.vatroutspotter.util.Clock
import com.kenkeremath.vatroutspotter.util.DefaultClock

@HiltViewModel
class StockingsViewModel @Inject constructor(
    private val stockingRepository: StockingRepository,
    private val fetchAndNotifyStockingsUseCase: FetchAndNotifyStockingsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock = DefaultClock(),
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Uninitialized)
    val uiState = _uiState.asStateFlow()
    private val _pagingState = MutableStateFlow<PagingState>(PagingState.Idle)
    val pagingState = _pagingState.asStateFlow()
    private val _filters = MutableStateFlow(StockingFilters())
    val filters = _filters.asStateFlow()
    private val _availableCounties = MutableStateFlow<List<String>>(emptyList())
    val availableCounties = _availableCounties.asStateFlow()

    // Use TreeSet to maintain sorted unique StockingInfo
    // TODO: centralize comparison logic
    private val allStockings = TreeSet<StockingInfo>(compareByDescending<StockingInfo?> { it?.date }
        .thenBy { it?.waterbody }.thenBy { it?.id })

    private var lastRefreshTime: Long = 0

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(ioDispatcher) {
            // Load cached data or display initial loading state
            val hasInitialData = stockingRepository.hasInitialData.first()
            if (hasInitialData) {
                loadSavedStockings()
                loadFilterOptions()
            } else {
                _uiState.value = HomeUiState.LoadingInitialData
            }

            fetchNewStockings()
        }
    }

    private suspend fun fetchNewStockings() {
        // Update last refresh time
        lastRefreshTime = clock.currentTimeMillis()
        
        // Fetch latest data
        val result = fetchAndNotifyStockingsUseCase.execute()
        val stockings = result.getOrNull()
        if (stockings == null) {
            // TODO: error handling
            _uiState.value = HomeUiState.Error(result.exceptionOrNull()?.message ?: "Error")
            return
        }
        loadFilterOptions()
        allStockings.addAll(stockings)
        if (_filters.value == StockingFilters()) {
            // Only update state with empty filters otherwise we might show invalid data
            _uiState.value = HomeUiState.Success(
                stockings = allStockings.toList()
            )
        }
    }

    private fun loadSavedStockings() {
        viewModelScope.launch(ioDispatcher) {
            val filters = _filters.value
            val loadResult = stockingRepository.loadSavedStockings(
                AppConfig.DEFAULT_PAGE_SIZE,
                filters
            )
            val page = loadResult.getOrNull()
            if (page == null) {
                // TODO: Error handling
                _uiState.value = HomeUiState.Error(loadResult.exceptionOrNull()?.message ?: "Error")
                return@launch
            }
            allStockings.addAll(page.stockings)
            if (!page.hasMore) {
                _pagingState.value = PagingState.ReachedEnd
            }
            if (allStockings.isEmpty()) {
                _uiState.value = HomeUiState.Empty
            } else {
                _uiState.value = HomeUiState.Success(
                    stockings = allStockings.toList()
                )
            }
        }
    }

    /**
     * Loads another page of stockings from the database. If we reach the end and have not yet
     * fetched historical data, we will attempt to fetch it.
     */
    fun loadMoreStockings() {
        val lastStocking = allStockings.lastOrNull()
        if (lastStocking == null ||
            (_pagingState.value is PagingState.Loading) ||
            (_pagingState.value is PagingState.ReachedEnd)
        ) {
            return
        }

        _pagingState.value = PagingState.Loading
        viewModelScope.launch {
            val result = stockingRepository.loadMoreSavedStockings(
                lastDate = lastStocking.date,
                lastWaterbody = lastStocking.waterbody,
                lastId = lastStocking.id,
                pageSize = AppConfig.DEFAULT_PAGE_SIZE,
                stockingFilters = _filters.value
            )

            val page = result.getOrNull()
            if (page == null) {
                _pagingState.value = PagingState.Error(result.exceptionOrNull())
                return@launch
            }

            if (!page.hasMore) {
                // Check if we've already loaded historical data
                if (stockingRepository.hasHistoricalData.first()) {
                    _pagingState.value = PagingState.ReachedEnd
                } else {
                    // If not, try to load historical data
                    val historicalResult = stockingRepository.fetchHistoricalData()
                    if (historicalResult.isSuccess) {
                        // Set state to Idle so pagination can continue
                        _pagingState.value = PagingState.Idle
                        return@launch
                    } else {
                        // If historical data load failed, we've reached the end
                        // TODO: error handling with retry
                        _pagingState.value = PagingState.ReachedEnd
                    }
                }
            } else {
                _pagingState.value = PagingState.Idle
            }

            allStockings.addAll(page.stockings)
            _uiState.value = HomeUiState.Success(
                stockings = allStockings.toList()
            )
        }
    }

    private suspend fun loadFilterOptions() {
        _availableCounties.value = stockingRepository.getAllCounties()
    }

    fun updateFilters(newFilters: StockingFilters) {
        _filters.value = newFilters
        // Clear existing stockings and reload with new filters
        allStockings.clear()
        _uiState.value = HomeUiState.Uninitialized
        _pagingState.value = PagingState.Idle
        loadSavedStockings()
    }

    fun clearFilters() {
        updateFilters(StockingFilters())
    }

    fun updateSearchTerm(searchTerm: String?) {
        val currentFilters = _filters.value
        val newFilters = currentFilters.copy(searchTerm = searchTerm?.takeIf { it.isNotBlank() })
        updateFilters(newFilters)
    }

    fun refreshStockings() {
        viewModelScope.launch {
            val currentTime = clock.currentTimeMillis()
            val timeSinceLastRefresh = currentTime - lastRefreshTime
            
            if (timeSinceLastRefresh < AppConfig.REFRESH_THROTTLE_MILLIS) {
                // If we're trying to refresh too soon, just show the refreshing indicator briefly
                _isRefreshing.value = true
                delay(AppConfig.REFRESH_THROTTLE_DELAY_MILLIS)
                _isRefreshing.value = false
                return@launch
            }
            
            // Proceed with actual refresh
            _isRefreshing.value = true
            fetchNewStockings()
            _isRefreshing.value = false
        }
    }
}

sealed class HomeUiState {
    data object Uninitialized : HomeUiState()
    data object LoadingInitialData : HomeUiState()
    data object Empty : HomeUiState()
    data class Success(val stockings: List<StockingInfo>) : HomeUiState()

    // TODO: Display error + retry
    data class Error(val message: String) : HomeUiState()
}

sealed class PagingState {
    object Idle : PagingState()
    object Loading : PagingState()
    object ReachedEnd : PagingState()
    data class Error(val exception: Throwable?) : PagingState()
} 