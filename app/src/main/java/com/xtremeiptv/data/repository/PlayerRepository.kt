package com.xtremeiptv.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
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
    private var currentUrl: String? = null
    
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
                Log.d(TAG, "Initializing ExoPlayer")
                exoPlayer = ExoPlayer.Builder(context).build()
                exoPlayer?.addListener(playerListener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Init failed", e)
            _error.value = "Player init failed: ${e.message}"
        }
    }
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        try {
            Log.d(TAG, "Loading stream: $url")
            
            if (url.isEmpty()) {
                _error.value = "Stream URL is empty"
                return
            }
            
            if (exoPlayer == null) {
                initPlayer()
            }
            
            if (exoPlayer == null) {
                _error.value = "Player not initialized"
                return
            }
            
            currentUrl = url
            
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)
                .setUserAgent("XtremeIPTV/1.0 (Android)")
            
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            val lowerUrl = url.lowercase()
            
            val mediaSource = when {
                lowerUrl.contains(".m3u8") || lowerUrl.contains("m3u8") || lowerUrl.contains("hls") -> {
                    Log.d(TAG, "Using HLS MediaSource")
                    HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(mediaItem)
                }
                lowerUrl.contains(".mpd") || lowerUrl.contains("dash") -> {
                    Log.d(TAG, "Using DASH MediaSource")
                    DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    Log.d(TAG, "Using Progressive MediaSource (MP4/TS)")
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
            }
            
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                seekTo(resumePosition)
                play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load failed", e)
            _error.value = "Load failed: ${e.message}"
        }
    }
    
    fun play() {
        try {
            exoPlayer?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Play failed", e)
        }
    }
    
    fun pause() {
        try {
            exoPlayer?.pause()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun seekTo(position: Long) {
        try {
            exoPlayer?.seekTo(position)
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        try {
            exoPlayer?.setPlaybackSpeed(speed)
            _playbackSpeed.value = speed
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun release() {
        try {
            Log.d(TAG, "Releasing player")
            exoPlayer?.release()
            exoPlayer = null
            currentUrl = null
            _error.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Release failed", e)
        }
    }
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _isPlaying.value = exoPlayer?.isPlaying == true
                    _buffering.value = false
                    _error.value = null
                    Log.d(TAG, "Player ready, isPlaying: ${exoPlayer?.isPlaying}")
                }
                Player.STATE_BUFFERING -> {
                    _buffering.value = true
                    Log.d(TAG, "Buffering")
                }
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                    Log.d(TAG, "Playback ended")
                }
                Player.STATE_IDLE -> {
                    _isPlaying.value = false
                    Log.d(TAG, "Player idle")
                }
            }
        }
        
        override fun onPositionDiscontinuity(reason: Int) {
            _currentPosition.value = exoPlayer?.currentPosition ?: 0
            _duration.value = exoPlayer?.duration ?: 0
        }
        
        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error", error)
            _error.value = error.message ?: "Playback error"
            _buffering.value = false
        }
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
}
