package com.example.trabajo1.ui.video

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.R
import com.example.trabajo1.ui.video_recorder.VideoViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoFragment : Fragment() {

    private lateinit var btnRecord: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnSwitch: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var previewView: androidx.camera.view.PreviewView

    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    private var camera: Camera? = null
    private var isFlashOn = false

    private val viewModel: VideoViewModel by viewModels()

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
        // Oculta la Toolbar del Activity (si existe)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        val root = inflater.inflate(R.layout.fragment_video, container, false)

        previewView = root.findViewById(R.id.previewView)
        btnRecord = root.findViewById(R.id.btnRecord)
        btnPause = root.findViewById(R.id.btnPause)
        btnSwitch = root.findViewById(R.id.btnSwitchCamera)
        btnFlash = root.findViewById(R.id.btnFlash)
        btnBack = root.findViewById(R.id.btnBack)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Estado inicial
        btnRecord.setImageResource(R.drawable.ic_record)
        btnPause.setImageResource(R.drawable.ic_pause)
        btnPause.isEnabled = false

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
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

                isFlashOn = false
                btnFlash.setImageResource(R.drawable.ic_flash_off)
                camera?.cameraControl?.enableTorch(false)

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

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "VIDEO_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MiAppVideos")
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

                        // Cambiamos botones al iniciar grabación
                        btnRecord.setImageResource(R.drawable.ic_stop)
                        btnPause.setImageResource(R.drawable.ic_pause)
                        btnPause.isEnabled = true

                        Toast.makeText(requireContext(), "Grabando...", Toast.LENGTH_SHORT).show()
                    }
                    is VideoRecordEvent.Finalize -> {
                        viewModel.setRecording(false)
                        viewModel.setPaused(false)

                        // Restauramos estado inicial
                        btnRecord.setImageResource(R.drawable.ic_record)
                        btnPause.setImageResource(R.drawable.ic_pause)
                        btnPause.isEnabled = false

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

        // Cambiamos icono de pausa a reanudar
        btnPause.setImageResource(R.drawable.ic_resume)

        Toast.makeText(requireContext(), "Grabación en pausa ⏸️", Toast.LENGTH_SHORT).show()
    }

    private fun resumeRecording() {
        recording?.resume()
        viewModel.setPaused(false)

        // Cambiamos icono de reanudar a pausa
        btnPause.setImageResource(R.drawable.ic_pause)

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
        val cam = camera ?: return

        isFlashOn = !isFlashOn
        cam.cameraControl.enableTorch(isFlashOn)

        if (isFlashOn) {
            btnFlash.setImageResource(R.drawable.ic_flash_on)
        } else {
            btnFlash.setImageResource(R.drawable.ic_flash_off)
        }
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Vuelve a mostrarla cuando salís del fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        cameraExecutor.shutdown()
    }
}