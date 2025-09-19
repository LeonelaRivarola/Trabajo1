package com.example.trabajo1.ui.precios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trabajo1.R

class PreciosFragment : Fragment() {

    private lateinit var recyclerIngenieria: RecyclerView
    private lateinit var recyclerOtros: RecyclerView
    private lateinit var adapterIngenieria: PreciosAdapter
    private lateinit var adapterOtros: PreciosAdapter

    private val viewModel: PreciosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_precios, container, false)

        recyclerIngenieria = view.findViewById(R.id.recyclerIngenieria)
        recyclerOtros = view.findViewById(R.id.recyclerOtros)

        adapterIngenieria = PreciosAdapter(mutableListOf())
        adapterOtros = PreciosAdapter(mutableListOf())

        recyclerIngenieria.layoutManager = LinearLayoutManager(requireContext())
        recyclerOtros.layoutManager = LinearLayoutManager(requireContext())

        recyclerIngenieria.adapter = adapterIngenieria
        recyclerOtros.adapter = adapterOtros

        // Observamos datos de Firebase
        viewModel.listaIngenieria.observe(viewLifecycleOwner) { lista ->
            adapterIngenieria.updateDatos(lista)
        }

        viewModel.listaOtros.observe(viewLifecycleOwner) { lista ->
            adapterOtros.updateDatos(lista)
        }

        return view
    }

    class PreciosAdapter(private val items: MutableList<PreciosViewModel.PrecioItem>) :
        RecyclerView.Adapter<PreciosAdapter.PrecioViewHolder>() {

        inner class PrecioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
            val txtValor: TextView = itemView.findViewById(R.id.txtValor)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrecioViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_precio, parent, false)
            return PrecioViewHolder(view)
        }

        override fun onBindViewHolder(holder: PrecioViewHolder, position: Int) {
            val item = items[position]
            holder.txtDescripcion.text = item.descripcion
            holder.txtValor.text = "$${item.valor_minimo} - $${item.valor_maximo}"
        }

        override fun getItemCount() = items.size

        fun updateDatos(nuevos: List<PreciosViewModel.PrecioItem>) {
            items.clear()
            items.addAll(nuevos)
            notifyDataSetChanged()
        }
    }
}
