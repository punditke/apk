package com.xtremeiptv.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "PlayerRepository"
        private const val CACHE_SIZE = 200 * 1024 * 1024L
    }
    
    private var exoPlayer: ExoPlayer? = null
    private var currentUrl: String? = null
    private var cache: SimpleCache? = null
    
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
        initCache()
        initPlayer()
    }
    
    private fun initCache() {
        try {
            val cacheDir = File(context.cacheDir, "media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE)
            cache = SimpleCache(cacheDir, evictor)
        } catch (e: Exception) {
            Log.e(TAG, "Cache init failed", e)
        }
    }
    
    private fun initPlayer() {
        try {
            if (exoPlayer == null) {
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(30000)
                    .setReadTimeoutMs(30000)
                    .setUserAgent("XtremeIPTV/2.0")
                
                val cacheDataSourceFactory = cache?.let {
                    CacheDataSource.Factory()
                        .setCache(it)
                        .setUpstreamDataSourceFactory(dataSourceFactory)
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                } ?: dataSourceFactory
                
                val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)
                
                exoPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setHandleAudioBecomingNoisy(true)
                    .build()
                    .apply {
                        addListener(playerListener)
                        videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Player init failed", e)
            _error.value = "Player init failed: ${e.message}"
        }
    }
    
    fun loadStream(url: String, resumePosition: Long = 0L) {
        try {
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
                .setConnectTimeoutMs(30000)
                .setReadTimeoutMs(30000)
                .setUserAgent("XtremeIPTV/2.0")
            
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            val lowerUrl = url.lowercase()
            
            val mediaSource = when {
                lowerUrl.contains(".m3u8") || lowerUrl.contains("m3u8") || lowerUrl.contains("hls") -> {
                    HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(mediaItem)
                }
                lowerUrl.contains(".mpd") || lowerUrl.contains("dash") -> {
                    DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
            }
            
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                if (resumePosition > 0) seekTo(resumePosition)
                play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load failed", e)
            _error.value = "Load failed: ${e.message}"
        }
    }
    
    fun play() = exoPlayer?.play()
    fun pause() = exoPlayer?.pause()
    fun seekTo(position: Long) = exoPlayer?.seekTo(position)
    
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }
    
    fun release() {
        try {
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
                }
                Player.STATE_BUFFERING -> _buffering.value = true
                Player.STATE_ENDED -> _isPlaying.value = false
                Player.STATE_IDLE -> _isPlaying.value = false
            }
        }
        
        override fun onPositionDiscontinuity(reason: Int) {
            _currentPosition.value = exoPlayer?.currentPosition ?: 0
            _duration.value = exoPlayer?.duration ?: 0
        }
        
        override fun onPlayerError(error: PlaybackException) {
            _error.value = when {
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                    "Network connection failed"
                error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                    "Stream not found"
                error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                    "Video format not supported"
                else -> error.message ?: "Playback error"
            }
            _buffering.value = false
        }
        
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            Log.d(TAG, "Video size: ${videoSize.width}x${videoSize.height}")
        }
        
        override fun onRenderedFirstFrame() {
            _buffering.value = false
        }
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
}
