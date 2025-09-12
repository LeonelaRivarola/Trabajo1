package com.example.trabajo1.ui.voice_recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trabajo1.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VoiceRecorderFragment : Fragment() {

    private lateinit var btnRecord: Button
    private lateinit var btnStop: Button
    private lateinit var tvTimer: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""

    private val viewModel: VoiceRecorderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_voice_recorder, container, false)

        btnRecord = root.findViewById(R.id.btnRecord)
        btnStop = root.findViewById(R.id.btnStop)
        tvTimer = root.findViewById(R.id.tvTimer)

        btnRecord.setOnClickListener { toggleRecording() }
        btnStop.setOnClickListener { stopRecording() }

        // Observamos cambios en el ViewModel
        viewModel.isRecording.observe(viewLifecycleOwner) { recording ->
            btnRecord.text = if (recording) "Pausar" else "Grabar"
            btnStop.isEnabled = recording
        }

        viewModel.recordingTime.observe(viewLifecycleOwner) { time ->
            tvTimer.text = time
        }

        return root
    }

    private fun toggleRecording() {
        if (viewModel.isRecording.value == true) {
            stopRecording()
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            } else {
                startRecording()
            }
        }
    }

    private fun startRecording() {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Toast.makeText(requireContext(), "Grabación guardada en: $outputFile", Toast.LENGTH_LONG).show()
        viewModel.setRecording(false)
    }
}