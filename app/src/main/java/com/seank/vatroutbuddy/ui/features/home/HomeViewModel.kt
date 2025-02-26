package com.seank.vatroutbuddy.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seank.vatroutbuddy.domain.model.StockingInfo
import com.seank.vatroutbuddy.data.repository.StockingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stockingRepository: StockingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStockings()
    }

    private fun loadStockings() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            // Get date 6 months ago
            val startDate = LocalDate.now().minusMonths(6)
            
            stockingRepository.getRecentStockings(startDate)
                .onSuccess { stockings ->
                    _uiState.value = HomeUiState.Success(stockings)
                }
                .onFailure { exception ->
                    _uiState.value = HomeUiState.Error(
                        exception.localizedMessage ?: "Failed to load stocking data"
                    )
                }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val stockings: List<StockingInfo>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 