package com.xtremeiptv.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
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
    
    companion object {
        private const val TAG = "PlayerRepository"
    }
    
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
    
    init {
        initPlayer()
    }
    
    private fun initPlayer() {
        try {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build()
                exoPlayer?.addListener(playerListener)
            }
        } catch (e: Exception) {
            _error.value = "Player init failed: ${e.message}"
        }
    }
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        try {
            if (exoPlayer == null) {
                initPlayer()
            }
            
            if (exoPlayer == null) {
                _error.value = "Player not initialized"
                return
            }
            
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)
                .setUserAgent("XtremeIPTV/1.0")
            
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                seekTo(resumePosition)
                play()
            }
        } catch (e: Exception) {
            _error.value = "Load failed: ${e.message}"
        }
    }
    
    fun play() {
        try {
            exoPlayer?.play()
        } catch (e: Exception) {
            _error.value = "Play failed: ${e.message}"
        }
    }
    
    fun pause() {
        try {
            exoPlayer?.pause()
        } catch (e: Exception) {
            // Ignore pause errors
        }
    }
    
    fun seekTo(position: Long) {
        try {
            exoPlayer?.seekTo(position)
        } catch (e: Exception) {
            // Ignore seek errors
        }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        try {
            exoPlayer?.setPlaybackSpeed(speed)
            _playbackSpeed.value = speed
        } catch (e: Exception) {
            // Ignore speed errors
        }
    }
    
    fun release() {
        try {
            exoPlayer?.release()
            exoPlayer = null
            _error.value = null
            _isPlaying.value = false
            _buffering.value = false
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
                    _error.value = null
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
