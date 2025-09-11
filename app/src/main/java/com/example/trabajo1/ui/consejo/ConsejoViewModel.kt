package com.example.trabajo1.ui.consejo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConsejoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is consejo Fragment"
    }
    val text: LiveData<String> = _text
}