package com.seank.vatroutbuddy.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seank.vatroutbuddy.data.repository.StockingRepository
import com.seank.vatroutbuddy.domain.model.StockingInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stockingRepository: StockingRepository,
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        observeStockings()
        refreshStockings()
    }

    private fun observeStockings() {
        viewModelScope.launch {
            combine(
                stockingRepository.getRecentStockings(LocalDate.now().minusMonths(6)),
                stockingRepository.getLastUpdateTime(),
                isRefreshing
            ) { stockings, lastUpdated, refreshing ->
                when {
                    refreshing -> HomeUiState.Loading
                    stockings.isEmpty() -> HomeUiState.Empty
                    else -> HomeUiState.Success(
                        stockings = stockings,
                        lastUpdatedAt = lastUpdated ?: LocalDateTime.now()
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refreshStockings() {
        isRefreshing.value = true
        viewModelScope.launch {
            stockingRepository.refreshStockings()
            isRefreshing.value = false
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Success(val stockings: List<StockingInfo>, val lastUpdatedAt: LocalDateTime) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 