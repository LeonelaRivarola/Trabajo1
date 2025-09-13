package com.example.trabajo1.ui.video_recorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoRecorderViewModel : ViewModel() {
    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> get() = _isRecording

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }
}