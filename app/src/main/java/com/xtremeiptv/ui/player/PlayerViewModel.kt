package com.xtremeiptv.ui.player

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
    
    val isPlaying = playerRepository.isPlaying
    val currentPosition = playerRepository.currentPosition
    val duration = playerRepository.duration
    val playbackSpeed = playerRepository.playbackSpeed
    val buffering = playerRepository.buffering
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentUrl: String? = null
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        if (currentUrl == url && !isPlaying.value) {
            // Same URL, just play
            playerRepository.play()
            return
        }
        
        currentUrl = url
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Clear error when playback starts
            launch {
                playerRepository.isPlaying.collect { playing ->
                    if (playing) {
                        _error.value = null
                        _isLoading.value = false
                    }
                }
            }
            
            // Collect errors
            launch {
                playerRepository.error.collect { err ->
                    if (err != null) {
                        _error.value = err
                        _isLoading.value = false
                    }
                }
            }
            
            playerRepository.loadStream(url, resumePosition)
        }
    }
    
    fun play() = playerRepository.play()
    fun pause() = playerRepository.pause()
    fun seekTo(position: Long) = playerRepository.seekTo(position)
    fun setPlaybackSpeed(speed: Float) = playerRepository.setPlaybackSpeed(speed)
    
    fun release() {
        playerRepository.release()
        currentUrl = null
    }
    
    fun getPlayer() = playerRepository.getPlayer()
    
    override fun onCleared() {
        release()
        super.onCleared()
    }
}
