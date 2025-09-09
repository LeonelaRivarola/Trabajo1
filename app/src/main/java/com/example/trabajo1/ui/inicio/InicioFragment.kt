package com.example.trabajo1.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.trabajo1.databinding.FragmentInicioBinding
import androidx.fragment.app.viewModels

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private val inicioViewModel: InicioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        // Observar cambios en LiveData
        inicioViewModel.dolar.observe(viewLifecycleOwner) { dolar ->
            dolar?.let {
                binding.txtDolarCompra.text = "Compra: ${it.compra}"
                binding.txtDolarVenta.text = "Venta: ${it.venta}"
                binding.txtDolarFecha.text = "Actualizado: ${it.fechaActualizacion}"
            }
        }

        // Llamar a la API
        inicioViewModel.cargarDolarOficial()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
