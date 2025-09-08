package com.example.trabajo1.ui.inicio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InicioViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Inicio Fragment"
    }
    val text: LiveData<String> = _text
}