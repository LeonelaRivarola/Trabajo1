package com.example.trabajo1.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.trabajo1.R
import com.google.firebase.database.*

class ListaFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val listaDatos = mutableListOf<String>()
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.listViewLugares)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listaDatos)
        listView.adapter = adapter

        // Inicializar referencia a Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("lugares")

        // Escuchar cambios en Firebase de forma segura
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaDatos.clear()
                if (snapshot.exists()) {
                    for (lugarSnapshot in snapshot.children) {
                        val direccion = lugarSnapshot.child("direccion").getValue(String::class.java) ?: "Sin dirección"
                        val referencia = lugarSnapshot.child("referencia").getValue(String::class.java) ?: "Sin referencia"
                        listaDatos.add("📍 $direccion\n📝 $referencia")
                    }
                } else {
                    listaDatos.add("No hay lugares guardados")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListaFragment", "Error al cargar lugares: ${error.message}")
                Toast.makeText(requireContext(), "Error al cargar lugares", Toast.LENGTH_SHORT).show()
                listaDatos.clear()
                listaDatos.add("Error al cargar datos")
                adapter.notifyDataSetChanged()
            }
        })
    }
}
