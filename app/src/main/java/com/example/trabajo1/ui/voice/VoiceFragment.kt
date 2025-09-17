package com.example.trabajo1.ui.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trabajo1.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VoiceFragment : Fragment() {

    private lateinit var btnRecord: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnBack: ImageButton

    private lateinit var tvTimer: Chronometer
    private var pauseOffset: Long = 0

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""

    private val viewModel: VoiceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Oculta la Toolbar del Activity (si existe)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        val root = inflater.inflate(R.layout.fragment_voice, container, false)

        btnRecord = root.findViewById(R.id.btnRecord)
        btnStop = root.findViewById(R.id.btnStop)
        btnPause = root.findViewById(R.id.btnPause)
        btnResume = root.findViewById(R.id.btnResume)
        btnBack = root.findViewById(R.id.btnBack)
        tvTimer = root.findViewById(R.id.tvTimer)

        // Estado inicial
        setInitialUI()

        // Eventos de botones
        btnRecord.setOnClickListener { startRecording() }
        btnStop.setOnClickListener { stopRecording() }
        btnPause.setOnClickListener { pauseRecording() }
        btnResume.setOnClickListener { resumeRecording() }
        btnBack.setOnClickListener { stopAndGoBack() }

        return root
    }

    private fun setInitialUI() {
        btnRecord.visibility = View.VISIBLE
        btnRecord.isEnabled = true

        btnStop.visibility = View.GONE
        btnStop.isEnabled = false

        btnPause.visibility = View.GONE
        btnPause.isEnabled = false

        btnResume.visibility = View.GONE
        btnResume.isEnabled = false
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
            return
        }

        val musicDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(musicDir, "AUDIO_$timeStamp.3gp")
        outputFile = audioFile.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else {
            MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                Toast.makeText(requireContext(), "Grabando...", Toast.LENGTH_SHORT).show()
                viewModel.setRecording(true)

                // Cronómetro
                tvTimer.base = SystemClock.elapsedRealtime() - pauseOffset
                tvTimer.start()

                // UI
                btnRecord.visibility = View.GONE
                btnRecord.isEnabled = false

                btnStop.visibility = View.VISIBLE
                btnStop.isEnabled = true

                btnPause.visibility = View.VISIBLE
                btnPause.isEnabled = true

                btnResume.visibility = View.GONE
                btnResume.isEnabled = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                Toast.makeText(requireContext(), "Grabación en pausa", Toast.LENGTH_SHORT).show()

                // Cronómetro
                tvTimer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - tvTimer.base

                // UI
                btnPause.visibility = View.GONE
                btnPause.isEnabled = false

                btnResume.visibility = View.VISIBLE
                btnResume.isEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Pausa no soportada en esta versión",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                Toast.makeText(requireContext(), "Grabación reanudada", Toast.LENGTH_SHORT).show()

                // Cronómetro
                tvTimer.base = SystemClock.elapsedRealtime() - pauseOffset
                tvTimer.start()

                // UI
                btnResume.visibility = View.GONE
                btnResume.isEnabled = false

                btnPause.visibility = View.VISIBLE
                btnPause.isEnabled = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Toast.makeText(
                requireContext(),
                "Grabación guardada en: $outputFile",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Cronómetro
        tvTimer.stop()
        tvTimer.base = SystemClock.elapsedRealtime()
        pauseOffset = 0

        // Reset UI
        setInitialUI()

        viewModel.setRecording(false)
    }

    private fun stopAndGoBack() {
        if (viewModel.isRecording.value == true) {
            stopRecording()
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Vuelve a mostrarla cuando salís del fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}