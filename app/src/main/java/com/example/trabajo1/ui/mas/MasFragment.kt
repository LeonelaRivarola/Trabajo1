package com.example.trabajo1.ui.mas

import com.example.trabajo1.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.databinding.FragmentMasBinding

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
        val voiceRecorderViewModel =
            ViewModelProvider(this).get(MasViewModel::class.java)

        _binding = FragmentMasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMas
        voiceRecorderViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnOpenRecorder.setOnClickListener {
            findNavController().navigate(R.id.action_masFragment_to_voiceRecorderFragment)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}