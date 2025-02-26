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
                isRefreshing
            ) { stockings, refreshing ->
                when {
                    refreshing -> HomeUiState.Loading
                    stockings.isEmpty() -> HomeUiState.Empty
                    else -> HomeUiState.Success(stockings)
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
                .onSuccess {
                    isRefreshing.value = false
                }
                .onFailure { e ->
                    // TODO: toast
                    isRefreshing.value = false
                    _uiState.value = HomeUiState.Error(e.message?: "Error")
                }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Empty : HomeUiState()
    data class Success(val stockings: List<StockingInfo>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 