package com.example.trabajo1.ui.medidorRA

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MedidorViewModel : ViewModel() {

    private val _distance = MutableLiveData<Float>()
    val distance: LiveData<Float> get() = _distance

    fun setDistance(value: Float) {
        _distance.value = value
    }
}