package com.example.trabajo1.ui.precios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class PreciosViewModel : ViewModel() {

    data class PrecioItem(
        val descripcion: String = "",
        val valor_minimo: String = "",
        val valor_maximo: String = ""
    )

    private val _listaIngenieria = MutableLiveData<List<PrecioItem>>(emptyList())
    val listaIngenieria: LiveData<List<PrecioItem>> = _listaIngenieria

    private val _listaOtros = MutableLiveData<List<PrecioItem>>(emptyList())
    val listaOtros: LiveData<List<PrecioItem>> = _listaOtros

    private val dbRefIngenieria: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("precios").child("ingenieria")
    }
    private val dbRefOtros: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("precios").child("otros")
    }

    private val listenerIngenieria = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val lista = snapshot.getValue(object : GenericTypeIndicator<List<PrecioItem>>() {}) ?: emptyList()
            _listaIngenieria.postValue(lista)
        }
        override fun onCancelled(error: DatabaseError) {}
    }

    private val listenerOtros = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val lista = snapshot.getValue(object : GenericTypeIndicator<List<PrecioItem>>() {}) ?: emptyList()
            _listaOtros.postValue(lista)
        }
        override fun onCancelled(error: DatabaseError) {}
    }

    init {
        dbRefIngenieria.addValueEventListener(listenerIngenieria)
        dbRefOtros.addValueEventListener(listenerOtros)
    }

    override fun onCleared() {
        super.onCleared()
        dbRefIngenieria.removeEventListener(listenerIngenieria)
        dbRefOtros.removeEventListener(listenerOtros)
    }
}
