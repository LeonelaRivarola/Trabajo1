package com.example.trabajo1.ui.mas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.databinding.FragmentMasBinding
import com.example.trabajo1.R


class MasFragment : Fragment() {

    private var _binding: FragmentMasBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val masViewModel = ViewModelProvider(this).get(MasViewModel::class.java)
        _binding = FragmentMasBinding.inflate(inflater, container, false)
//        val root: View = binding.root

        //observa el texto de viewmodel
//        masViewModel.text.observe(viewLifecycleOwner) { text ->
//            binding.textMas.text = text
//        }


        //conf de los botones
        binding.btnMapa.setOnClickListener {
            //navega al mapfragment
            findNavController().navigate(R.id.action_navigation_mas_to_mapFragment)
        }

//        binding.btnGrabadora.setOnClickListener {
//            binding.textMas.text = "Grabadora"
//        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}