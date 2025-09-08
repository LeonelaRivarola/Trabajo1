package com.example.trabajo1.ui.mas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MasViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mas Fragment"
    }
    val text: LiveData<String> = _text
}