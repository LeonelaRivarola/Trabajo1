package com.example.trabajo1.ui.inicio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.trabajo1.databinding.FragmentInicioBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trabajo1.R
import com.example.trabajo1.databinding.ItemNoticiaBinding

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!
    private val inicioViewModel: InicioViewModel by viewModels()

    private lateinit var noticiasAdapter: NoticiasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        // --- Dólar ---
        inicioViewModel.dolar.observe(viewLifecycleOwner) { dolar ->
            dolar?.let {
                binding.txtDolarCompra.text = "Compra: ${it.compra}"
                binding.txtDolarVenta.text = "Venta: ${it.venta}"
                binding.txtDolarFecha.text = "Actualizado: ${it.fechaActualizacion}"
            }
        }
        inicioViewModel.cargarDolarOficial()

        // --- Noticias ---
        binding.recyclerNoticias.layoutManager = LinearLayoutManager(requireContext())
        noticiasAdapter = NoticiasAdapter(emptyList())
        binding.recyclerNoticias.adapter = noticiasAdapter

        inicioViewModel.noticias.observe(viewLifecycleOwner) { lista ->
            noticiasAdapter.updateData(lista)
            binding.swipeRefreshLayoutNoticias.isRefreshing = false
        }

        // Primera carga de datos (solo si aún no hay noticias)
        inicioViewModel.cargarNoticias(force = false)

        // Swipe-to-refresh sí fuerza recarga
        binding.swipeRefreshLayoutNoticias.setOnRefreshListener {
            inicioViewModel.cargarNoticias(force = true)
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Adapter interno ---
    inner class NoticiasAdapter(private var noticias: List<InicioViewModel.Noticia>) :
        RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder>() {

        inner class NoticiaViewHolder(val binding: ItemNoticiaBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
            val binding = ItemNoticiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return NoticiaViewHolder(binding)
        }

        override fun getItemCount() = noticias.size

        override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
            val noticia = noticias[position]
            holder.binding.txtTitulo.text = noticia.title
            holder.binding.txtFuente.text = noticia.source.name
            holder.binding.txtFecha.text = noticia.publishedAt.take(10) // YYYY-MM-DD
            Glide.with(holder.binding.imgNoticia.context)
                .load(noticia.image)
                .placeholder(R.drawable.ic_placeholder_24dp)
                .into(holder.binding.imgNoticia)

            holder.binding.root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url))
                holder.binding.root.context.startActivity(intent)
            }
        }

        fun updateData(newList: List<InicioViewModel.Noticia>) {
            noticias = newList
            notifyDataSetChanged()
        }
    }
}