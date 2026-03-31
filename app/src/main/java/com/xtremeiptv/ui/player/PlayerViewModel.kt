package com.xtremeiptv.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "PlayerViewModel"
    }
    
    val isPlaying = playerRepository.isPlaying
    val currentPosition = playerRepository.currentPosition
    val duration = playerRepository.duration
    val playbackSpeed = playerRepository.playbackSpeed
    val buffering = playerRepository.buffering
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        Log.d(TAG, "ViewModel created")
    }
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        viewModelScope.launch {
            Log.d(TAG, "loadStream called with url: $url")
            _isLoading.value = true
            _error.value = null
            
            // Collect errors
            launch {
                playerRepository.error.collect { err ->
                    if (err != null) {
                        Log.e(TAG, "Error from repository: $err")
                        _error.value = err
                        _isLoading.value = false
                    }
                }
            }
            
            // Start loading
            playerRepository.loadStream(url, resumePosition)
            
            // Wait for playback to start
            launch {
                playerRepository.isPlaying.collect { playing ->
                    if (playing) {
                        Log.d(TAG, "Playback started")
                        _isLoading.value = false
                    }
                }
            }
        }
    }
    
    fun play() {
        Log.d(TAG, "play() called")
        playerRepository.play()
    }
    
    fun pause() {
        Log.d(TAG, "pause() called")
        playerRepository.pause()
    }
    
    fun seekTo(position: Long) {
        Log.d(TAG, "seekTo: $position")
        playerRepository.seekTo(position)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        Log.d(TAG, "setPlaybackSpeed: $speed")
        playerRepository.setPlaybackSpeed(speed)
    }
    
    fun release() {
        Log.d(TAG, "release() called")
        playerRepository.release()
    }
    
    fun getPlayer() = playerRepository.getPlayer()
    
    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared")
        release()
        super.onCleared()
    }
}
