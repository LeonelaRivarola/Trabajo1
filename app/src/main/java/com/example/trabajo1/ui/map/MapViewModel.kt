package com.example.trabajo1.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private val _direccion = MutableLiveData<String>()
    val direccion: LiveData<String> get() = _direccion

    fun actualizarDireccion(nuevaDireccion: String) {
        _direccion.value = nuevaDireccion
    }
}