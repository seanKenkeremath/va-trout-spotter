package com.seank.vatroutbuddy.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seank.vatroutbuddy.data.repository.NotificationSubscriptionRepository
import com.seank.vatroutbuddy.data.repository.StockingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val subscriptionRepository: NotificationSubscriptionRepository,
    private val stockingRepository: StockingRepository
) : ViewModel() {

    private val _counties = MutableStateFlow<List<String>>(emptyList())
    private val _waterbodies = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<NotificationsUiState> = combine(
        _counties,
        _waterbodies,
        subscriptionRepository.getCountySubscriptions(),
        subscriptionRepository.getWaterbodySubscriptions()
    ) { counties, waterbodies, countySubscriptions, waterbodySubscriptions ->
        NotificationsUiState(
            counties = counties,
            waterbodies = waterbodies,
            subscribedCounties = countySubscriptions.map { it.value }.toSet(),
            subscribedWaterbodies = waterbodySubscriptions.map { it.value }.toSet()
        )
    }.stateIn(
        scope = viewModelScope,
        // Keep the state alive while the view model is alive
        // helps with config changes for instance
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationsUiState()
    )

    init {
        loadAvailableOptions()
    }

    private fun loadAvailableOptions() {
        viewModelScope.launch {
            _counties.value = stockingRepository.getAllCounties()
            // For waterbodies, we might want to limit to recent ones or implement search
            // This is just a placeholder implementation
            _waterbodies.value = stockingRepository.getAllWaterbodies()
        }
    }

    fun toggleCountySubscription(county: String, subscribe: Boolean) {
        viewModelScope.launch {
            subscriptionRepository.toggleCountySubscription(county, subscribe)
        }
    }

    fun toggleWaterbodySubscription(waterbody: String, subscribe: Boolean) {
        viewModelScope.launch {
            subscriptionRepository.toggleWaterbodySubscription(waterbody, subscribe)
        }
    }
}

data class NotificationsUiState(
    val counties: List<String> = emptyList(),
    val waterbodies: List<String> = emptyList(),
    val subscribedCounties: Set<String> = emptySet(),
    val subscribedWaterbodies: Set<String> = emptySet()
) 