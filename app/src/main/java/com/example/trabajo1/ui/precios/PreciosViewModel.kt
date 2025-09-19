package com.example.trabajo1.ui.precios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreciosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is precios Fragment"
    }
    val text: LiveData<String> = _text
}