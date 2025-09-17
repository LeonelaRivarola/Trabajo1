package com.example.trabajo1.ui.voice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VoiceViewModel : ViewModel() {

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _recordingTime = MutableLiveData("00:00:00")
    val recordingTime: LiveData<String> get() = _recordingTime

    fun setRecording(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    fun updateTime(time: String) {
        _recordingTime.value = time
    }
}