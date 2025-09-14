package com.example.trabajo1.ui.inicio

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.example.trabajo1.databinding.FragmentInicioBinding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.trabajo1.R
import com.example.trabajo1.databinding.ItemNoticiaBinding

class InicioFragment : Fragment(){

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private val inicioViewModel: InicioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        //Observar dolar
        inicioViewModel.dolar.observe(viewLifecycleOwner) { dolar ->
            dolar?.let {
                binding.txtDolarCompra.text = "Compra: ${it.compra}"
                binding.txtDolarVenta.text = "Venta: ${it.venta}"
                binding.txtDolarFecha.text = "Fecha: ${it.fechaActualizacion}"
            }
        }

        //Llamar API
        inicioViewModel.cargarDolarOficial()

        //Codigo para las noticias:
        //Inicializar RecyclerView
        binding.recyclerNoticias.layoutManager = LinearLayoutManager(requireContext())

        //Observar noticias
        inicioViewModel.noticias.observe(viewLifecycleOwner) { lista ->
                binding.recyclerNoticias.adapter = NoticiasAdapter(lista)
        }

        //Cargar noticias
        inicioViewModel.cargarNoticias()

        return  binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Adapter interno
    inner class NoticiasAdapter(private val noticias: List<InicioViewModel.Noticia>) :
            RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder>(){

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
                .into(holder.binding.imgNoticia)

            holder.binding.root.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(noticia.url))
                holder.binding.root.context.startActivity(intent)
            }
        }

            }
}