package com.example.trabajo1.ui.medidorAR

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.R
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.sqrt

class MedidorARFragment : Fragment(R.layout.fragment_medida) {

//    private var arFragment: ArFragment? = null
    private lateinit var tvDistance: TextView
    private val placedAnchors = mutableListOf<Anchor>()
    private lateinit var arFragment: ArFragment
    private lateinit var btnBack: ImageButton
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvDistance = view.findViewById(R.id.tvDistance)

        // Buscar el ArFragment en el layout
        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment


        // Oculta la Toolbar del Activity (si existe) en el oncreatedview
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        if (hasCameraPermission()) {
            setupAr()
        } else {
            // pide permiso de cámara
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAr() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            // Limpiar si ya hay dos puntos
            if (placedAnchors.size >= 2) {
                placedAnchors.forEach { it.detach() }
                placedAnchors.clear()
            }
            val anchor = hitResult.createAnchor()
            placedAnchors.add(anchor)

            placeMarker(anchor)

            if (placedAnchors.size == 2) {
                val d = distanceBetweenAnchors(placedAnchors[0], placedAnchors[1])
                tvDistance.text = String.format("Distancia: %.2f m", d)
                drawLineBetween(placedAnchors[0], placedAnchors[1])
            } else {
                tvDistance.text = "Toca el segundo punto..."
            }
        }
    }
    private fun placeMarker(anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        MaterialFactory.makeOpaqueWithColor(requireContext(), Color(0f, 1f, 0f))
            .thenAccept { material ->
                val sphere = ShapeFactory.makeSphere(0.02f, Vector3(0f, 0.02f, 0f), material)
                val node = Node()
                node.setParent(anchorNode)
                node.renderable = sphere
            }
    }

    private fun distanceBetweenAnchors(a1: Anchor, a2: Anchor): Float {
        val p1 = a1.pose
        val p2 = a2.pose
        val dx = p1.tx() - p2.tx()
        val dy = p1.ty() - p2.ty()
        val dz = p1.tz() - p2.tz()
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun drawLineBetween(a1: Anchor, a2: Anchor) {
        val p1 = a1.pose
        val p2 = a2.pose
        val midX = (p1.tx() + p2.tx()) / 2f
        val midY = (p1.ty() + p2.ty()) / 2f
        val midZ = (p1.tz() + p2.tz()) / 2f

        val session = arFragment.arSceneView.session ?: return
        val midPose = Pose(floatArrayOf(midX, midY, midZ), floatArrayOf(0f, 0f, 0f, 1f))
        val midAnchor = session.createAnchor(midPose)
        val anchorNode = AnchorNode(midAnchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        val distance = distanceBetweenAnchors(a1, a2)
        MaterialFactory.makeOpaqueWithColor(requireContext(), Color(0f, 0f, 1f))
            .thenAccept { material ->
                val thickness = 0.005f
                val cube = ShapeFactory.makeCube(Vector3(distance, thickness, thickness), Vector3.zero(), material)
                val node = Node()
                node.setParent(anchorNode)
                node.renderable = cube
                val dir = Vector3(p2.tx() - p1.tx(), p2.ty() - p1.ty(), p2.tz() - p1.tz())
                node.worldRotation = Quaternion.lookRotation(dir, Vector3(0f, 1f, 0f))
            }
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAr()
            } else {
                tvDistance.text = "Permiso de cámara denegado"
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroyView() {
        placedAnchors.forEach { it.detach() }
        placedAnchors.clear()
        super.onDestroyView()
    }
}