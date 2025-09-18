package com.example.trabajo1.ui.medidorRA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trabajo1.R
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.ModelRenderable

class MedidorFragment : Fragment() {

    private lateinit var arFragment: ArFragment
    private lateinit var tvDistance: TextView
    private val viewModel: MedidorViewModel by viewModels()

    private val anchors = mutableListOf<AnchorNode>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_medidor, container, false)
        tvDistance = view.findViewById(R.id.tvDistance)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Observar distancia en el ViewModel
        viewModel.distance.observe(viewLifecycleOwner) {
            tvDistance.text = "Distancia: %.2f m".format(it)
        }

        // Tap en la pantalla para colocar puntos
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _, _ ->
            if (anchors.size == 2) {
                // Reiniciar si ya hay 2 puntos
                anchors.forEach { it.anchor?.detach() }
                anchors.clear()
            }

            // Crear anchor y esfera visual
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.setParent(arFragment.arSceneView.scene)
            placeSphere(anchorNode)

            anchors.add(anchorNode)

            if (anchors.size == 2) {
                val p1 = anchors[0].worldPosition
                val p2 = anchors[1].worldPosition
                val distance = Vector3.subtract(p1, p2).length()
                viewModel.setDistance(distance)
            }
        }
    }

    // Dibuja una pequeña esfera en el punto seleccionado
    private fun placeSphere(anchorNode: AnchorNode) {
        ModelRenderable.builder()
            .setSource(requireContext(), R.raw.sphere) // esfera en assets
            .build()
            .thenAccept { renderable ->
                anchorNode.renderable = renderable
            }
    }
}