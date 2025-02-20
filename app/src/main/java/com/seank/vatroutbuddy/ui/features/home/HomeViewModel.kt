package com.seank.vatroutbuddy.ui.features.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seank.vatroutbuddy.data.repository.StockingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stockingRepository: StockingRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the home screen"
    }
    val text: LiveData<String> = _text

    val recentStockings = stockingRepository.getRecentStockings()
} 