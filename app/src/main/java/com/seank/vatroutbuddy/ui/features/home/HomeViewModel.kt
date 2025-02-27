package com.seank.vatroutbuddy.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seank.vatroutbuddy.data.repository.StockingRepository
import com.seank.vatroutbuddy.domain.model.StockingInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.TreeSet
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stockingRepository: StockingRepository,
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // Use TreeSet to maintain sorted unique StockingInfo
    private val allStockings = TreeSet<StockingInfo>(compareByDescending<StockingInfo?> { it?.date }
        .thenBy { it?.county })
    private val latestStockingDateRangeStart = LocalDate.now().minusMonths(PAGE_SIZE_MONTHS)

    // Keep track of loading state for pagination
    private val _pageLoading = MutableStateFlow(false)
    val pageLoading = _pageLoading.asStateFlow()

    init {
        observeStockings()
        refreshStockings()
    }

    private fun observeStockings() {
        viewModelScope.launch {
            combine(
                stockingRepository.getRecentStockings(latestStockingDateRangeStart),
                isRefreshing
            ) { stockings, isRefreshing ->
                when {
                    isRefreshing -> HomeUiState.Loading
                    stockings.isEmpty() -> HomeUiState.Empty
                    else -> {
                        allStockings.addAll(stockings) // TreeSet will handle deduplication and sorting
                        HomeUiState.Success(
                            stockings = allStockings.toList()
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refreshStockings() {
        isRefreshing.value = true
        viewModelScope.launch {
            stockingRepository.refreshSinceLastStocking()
            isRefreshing.value = false
        }
    }

    fun loadMoreStockings() {
        if (_pageLoading.value || allStockings.isEmpty()) return

        viewModelScope.launch {
            val oldestStockingDate = allStockings.last().date
            val count = stockingRepository.countStockingsBeforeDate(oldestStockingDate)

            if (count <= 0) {
                // No more stockings to load
                return@launch
            }

            _pageLoading.value = true
            delay(1000L) // Simulated delay
            val result = stockingRepository.loadStockingsBeforeDate(
                date = oldestStockingDate,
                limit = 30
            )
            if (result.isSuccess) {
                allStockings.addAll(result.getOrDefault(emptyList()))
                _uiState.value = HomeUiState.Success(allStockings.toList())
            }
            _pageLoading.value = false // Reset loading state
        }
    }

    companion object {
        private const val PAGE_SIZE_MONTHS = 3L
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Success(val stockings: List<StockingInfo>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 