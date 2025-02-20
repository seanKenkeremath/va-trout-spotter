package com.seank.vatroutbuddy.ui.features.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the locations screen"
    }
    val text: LiveData<String> = _text
} 