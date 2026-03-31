package com.xtremeiptv.data.repository

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val context: Context
) {
    
    private var exoPlayer: ExoPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _buffering = MutableStateFlow(false)
    val buffering: StateFlow<Boolean> = _buffering.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    fun initializePlayer() {
        if (exoPlayer == null) {
            try {
                exoPlayer = ExoPlayer.Builder(context)
                    .build()
                    .apply {
                        addListener(playerListener)
                    }
                _isReady.value = true
            } catch (e: Exception) {
                _error.value = "Player initialization failed: ${e.message}"
            }
        }
    }
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        try {
            if (exoPlayer == null) {
                initializePlayer()
            }
            
            if (exoPlayer == null) {
                _error.value = "Player not initialized"
                return
            }
            
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(10000)
                .setReadTimeoutMs(10000)
            
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
            
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                seekTo(resumePosition)
                play()
            }
        } catch (e: Exception) {
            _error.value = "Failed to load stream: ${e.message}"
        }
    }
    
    fun play() {
        exoPlayer?.play()
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }
    
    fun release() {
        try {
            exoPlayer?.release()
            exoPlayer = null
            _isReady.value = false
            _isPlaying.value = false
            _buffering.value = false
            _error.value = null
        } catch (e: Exception) {
            // Ignore release errors
        }
    }
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _isPlaying.value = exoPlayer?.isPlaying == true
                    _buffering.value = false
                }
                Player.STATE_BUFFERING -> {
                    _buffering.value = true
                }
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                }
                Player.STATE_IDLE -> {
                    _isPlaying.value = false
                }
            }
        }
        
        override fun onPositionDiscontinuity(reason: Int) {
            _currentPosition.value = exoPlayer?.currentPosition ?: 0
            _duration.value = exoPlayer?.duration ?: 0
        }
        
        override fun onPlayerError(error: PlaybackException) {
            _error.value = error.message ?: "Playback error"
            _buffering.value = false
        }
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
}
