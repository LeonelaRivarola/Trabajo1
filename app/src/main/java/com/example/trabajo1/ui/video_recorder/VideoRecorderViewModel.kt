package com.example.trabajo1.ui.video_recorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoRecorderViewModel : ViewModel() {
    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _isPaused = MutableLiveData(false)
    val isPaused: LiveData<Boolean> get() = _isPaused

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }
}