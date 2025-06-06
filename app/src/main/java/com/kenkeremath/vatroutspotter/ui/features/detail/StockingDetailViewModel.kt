package com.kenkeremath.vatroutspotter.ui.features.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenkeremath.vatroutspotter.data.repository.StockingRepository
import com.kenkeremath.vatroutspotter.domain.model.StockingInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = StockingDetailViewModel.Factory::class)
class StockingDetailViewModel @AssistedInject constructor(
    @Assisted private val stocking: StockingInfo,
    private val repository: StockingRepository,
) : ViewModel() {

    private val _locationName = MutableStateFlow<String>(stocking.waterbody)
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _uiState = MutableStateFlow<StockingDetailUiState>(StockingDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadRelatedStockings()
    }

    private fun loadRelatedStockings() {
        viewModelScope.launch {
            _uiState.value = StockingDetailUiState.Loading

            try {
                val relatedStockings = repository.getStockingsByWaterbody(
                    stocking.waterbody,
                    limit = 10,
                ).getOrThrow()

                _uiState.value = StockingDetailUiState.Success(
                    stocking = stocking,
                    relatedStockings = relatedStockings
                )
            } catch (e: Exception) {
                _uiState.value = StockingDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retry() {
        loadRelatedStockings()
    }

    @AssistedFactory
    interface Factory {
        fun create(stocking: StockingInfo): StockingDetailViewModel
    }
}

sealed class StockingDetailUiState {
    data object Loading : StockingDetailUiState()
    data class Success(
        val stocking: StockingInfo,
        val relatedStockings: List<StockingInfo>
    ) : StockingDetailUiState()

    data class Error(val message: String) : StockingDetailUiState()
} 