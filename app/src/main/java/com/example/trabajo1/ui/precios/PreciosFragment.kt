package com.example.trabajo1.ui.precios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.trabajo1.databinding.FragmentPreciosBinding

class PreciosFragment : Fragment() {

    private var _binding: FragmentPreciosBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val preciosViewModel =
            ViewModelProvider(this).get(PreciosViewModel::class.java)

        _binding = FragmentPreciosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPrecios
        preciosViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}