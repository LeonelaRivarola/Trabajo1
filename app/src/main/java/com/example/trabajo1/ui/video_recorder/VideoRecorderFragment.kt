package com.example.trabajo1.ui.video_recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoRecorderFragment : Fragment() {

    private lateinit var btnRecord: Button
    private lateinit var btnPause: Button
    private lateinit var btnSwitch: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var previewView: androidx.camera.view.PreviewView

    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    private val viewModel: VideoRecorderViewModel by viewModels()

    // Gestión de permisos
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Se necesitan permisos de cámara y audio", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_video_recorder, container, false)

        previewView = root.findViewById(R.id.previewView)
        btnRecord = root.findViewById(R.id.btnRecord)
        btnPause = root.findViewById(R.id.btnPause)
        btnSwitch = root.findViewById(R.id.btnSwitchCamera)
        btnFlash = root.findViewById(R.id.btnFlash)
        btnBack = root.findViewById(R.id.btnBack)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Ocultamos el botón de pausar al inicio
        btnPause.isEnabled = false

        // ✅ Comprobar permisos antes de iniciar la cámara
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionsLauncher.launch(requiredPermissions)
        }

        btnRecord.setOnClickListener { toggleRecording() }
        btnPause.setOnClickListener { togglePauseResume() }
        btnSwitch.setOnClickListener { switchCamera() }
        btnFlash.setOnClickListener { toggleFlash() }
        btnBack.setOnClickListener {
            stopRecording()
            findNavController().navigateUp()
        }

        return root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun toggleRecording() {
        if (viewModel.isRecording.value == true) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun togglePauseResume() {
        if (viewModel.isPaused.value == true) {
            resumeRecording()
        } else {
            pauseRecording()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return

        if (!allPermissionsGranted()) {
            requestPermissionsLauncher.launch(requiredPermissions)
            return
        }

        // Uso MediaStore para guardar en la galeria del celular
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "VIDEO_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MiAppVideos")
            // 👆 Carpeta visible en galería
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutput)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(requireContext())) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        viewModel.setRecording(true)
                        viewModel.setPaused(false)
                        btnRecord.text = "Detener"
                        btnPause.isEnabled = true
                        btnPause.text = "Pausar"
                        Toast.makeText(requireContext(), "Grabando...", Toast.LENGTH_SHORT).show()
                    }
                    is VideoRecordEvent.Finalize -> {
                        viewModel.setRecording(false)
                        viewModel.setPaused(false)
                        btnRecord.text = "Grabar"
                        btnPause.isEnabled = false
                        btnPause.text = "Pausar"

                        if (event.hasError()) {
                            Toast.makeText(requireContext(), "Error al guardar video", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Video guardado en galería", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    private fun pauseRecording() {
        recording?.pause()
        viewModel.setPaused(true)
        btnPause.text = "Reanudar"
        Toast.makeText(requireContext(), "Grabación en pausa ⏸️", Toast.LENGTH_SHORT).show()
    }

    private fun resumeRecording() {
        recording?.resume()
        viewModel.setPaused(false)
        btnPause.text = "Pausar"
        Toast.makeText(requireContext(), "Grabación reanudada ▶️", Toast.LENGTH_SHORT).show()
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        startCamera()
    }

    private fun toggleFlash() {
        Toast.makeText(requireContext(), "Flash toggle (pendiente)", Toast.LENGTH_SHORT).show()
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}