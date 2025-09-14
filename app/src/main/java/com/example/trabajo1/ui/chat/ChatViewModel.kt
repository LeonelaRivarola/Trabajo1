package com.example.trabajo1.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class ChatViewModel : ViewModel() {

    data class Mensaje(
        val texto: String? = "",
        val remitente: String? = "usuario"
    )

    private val _mensajes = MutableLiveData<MutableList<Mensaje>>(mutableListOf())
    val mensajes: LiveData<MutableList<Mensaje>> = _mensajes

    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("chat")
    }

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            try {
                val mensaje = snapshot.getValue(Mensaje::class.java)
                mensaje?.let {
                    val lista = _mensajes.value ?: mutableListOf()
                    lista.add(it)
                    _mensajes.postValue(lista)
                }
            } catch (e: DatabaseException) {
                // Por si hay datos mal formateados
                e.printStackTrace()
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    init {
        database.addChildEventListener(childEventListener)
    }

    fun enviarMensaje(texto: String) {
        val mensajeTrimmed = texto.trim()
        if (mensajeTrimmed.isNotEmpty()) {
            val mensaje = Mensaje(mensajeTrimmed, "usuario")
            database.push().setValue(mensaje)
        }
    }

    override fun onCleared() {
        super.onCleared()
        database.removeEventListener(childEventListener)
    }
}