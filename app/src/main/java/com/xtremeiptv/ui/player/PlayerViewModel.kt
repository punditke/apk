package com.xtremeiptv.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.xtremeiptv.data.repository.PlayerRepository
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _streamUrl = MutableStateFlow<String?>(null)
    val streamUrl: StateFlow<String?> = _streamUrl.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val isPlaying = playerRepository.isPlaying
    val currentPosition = playerRepository.currentPosition
    val duration = playerRepository.duration
    val playbackSpeed = playerRepository.playbackSpeed
    
    fun loadContent(contentId: String, contentType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val profile = profileRepository.getActiveProfile().firstOrNull()
                if (profile == null) {
                    _error.value = "No active profile"
                    _isLoading.value = false
                    return@launch
                }
                
                // Get stream URL based on content type and ID
                // This would fetch from actual data source
                val url = when (contentType) {
                    "live" -> "https://example.com/live/$contentId"
                    "movie" -> "https://example.com/movie/$contentId"
                    "series" -> "https://example.com/series/$contentId"
                    else -> null
                }
                
                if (url == null) {
                    _error.value = "Stream URL not found"
                } else {
                    _streamUrl.value = url
                    playerRepository.loadStream(url)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getPlayer(): Player? = playerRepository.getPlayer()
    
    fun play() = playerRepository.play()
    fun pause() = playerRepository.pause()
    fun seekTo(position: Long) = playerRepository.seekTo(position)
    fun setPlaybackSpeed(speed: Float) = playerRepository.setPlaybackSpeed(speed)
    
    override fun onCleared() {
        super.onCleared()
        playerRepository.release()
    }
}
