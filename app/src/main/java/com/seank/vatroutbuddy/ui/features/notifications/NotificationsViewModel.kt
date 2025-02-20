package com.seank.vatroutbuddy.ui.features.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the notifications screen"
    }
    val text: LiveData<String> = _text
} 