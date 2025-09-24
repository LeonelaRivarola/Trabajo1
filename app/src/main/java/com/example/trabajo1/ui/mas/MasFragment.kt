package com.example.trabajo1.ui.mas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.databinding.FragmentMasBinding
import com.example.trabajo1.R


class MasFragment : Fragment() {

    private var _binding: FragmentMasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMasBinding.inflate(inflater, container, false)



        //conf de los botones
        binding.btnMapa.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mas_to_mapFragment)
        }
        binding.btnVideo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mas_to_videoFragment)
        }
        binding.btnGrabadora.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mas_to_voiceFragment)
        }
        binding.btnMedidorAR.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mas_to_medidorFragment)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_mas_to_configFragment)
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}