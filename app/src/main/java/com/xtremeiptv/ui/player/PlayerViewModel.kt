package com.xtremeiptv.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
    
    fun initializePlayer() {
        playerRepository.initializePlayer(context)
    }
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            playerRepository.loadStream(url, resumePosition)
            
            playerRepository.error.collect { err ->
                _error.value = err
                _isLoading.value = false
            }
            
            playerRepository.isPlaying.collect { playing ->
                if (playing) _isLoading.value = false
            }
        }
    }
    
    fun play() = playerRepository.play()
    fun pause() = playerRepository.pause()
    fun seekTo(position: Long) = playerRepository.seekTo(position)
    fun setPlaybackSpeed(speed: Float) = playerRepository.setPlaybackSpeed(speed)
    fun release() = playerRepository.release()
    
    fun getPlayer() = playerRepository.getPlayer()
}
