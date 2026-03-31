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
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.common.util.UnstableApi
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
        private const val CACHE_SIZE = 200 * 1024 * 1024L // 200MB cache
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
            Log.d(TAG, "Cache initialized at: ${cacheDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Cache init failed", e)
        }
    }
    
    private fun initPlayer() {
        try {
            if (exoPlayer == null) {
                Log.d(TAG, "Initializing ExoPlayer")
                
                // Create data source factory for network streams
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(30000)
                    .setReadTimeoutMs(30000)
                    .setUserAgent("XtremeIPTV/2.0 (Android; ExoPlayer)")
                
                // Create cache data source
                val cacheDataSourceFactory = cache?.let {
                    CacheDataSource.Factory()
                        .setCache(it)
                        .setUpstreamDataSourceFactory(dataSourceFactory)
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                } ?: dataSourceFactory
                
                // Create media source factory
                val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)
                    .setLiveTargetOffsetMs(5000)
                    .setLiveMinLoadableRetryCount(3)
                
                exoPlayer = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setHandleAudioBecomingNoisy(true)
                    .build()
                    .apply {
                        addListener(playerListener)
                        videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                    }
                
                Log.d(TAG, "ExoPlayer initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Player init failed", e)
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
                .setConnectTimeoutMs(30000)
                .setReadTimeoutMs(30000)
                .setUserAgent("XtremeIPTV/2.0 (Android; ExoPlayer)")
            
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            val lowerUrl = url.lowercase()
            
            val mediaSource = when {
                lowerUrl.contains(".m3u8") || lowerUrl.contains("m3u8") || 
                lowerUrl.contains("hls") || lowerUrl.contains(".m3u") -> {
                    Log.d(TAG, "Creating HLS MediaSource")
                    HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(mediaItem)
                }
                lowerUrl.contains(".mpd") || lowerUrl.contains("dash") -> {
                    Log.d(TAG, "Creating DASH MediaSource")
                    DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    Log.d(TAG, "Creating Progressive MediaSource (MP4/TS)")
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
            }
            
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                if (resumePosition > 0) {
                    seekTo(resumePosition)
                }
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
            Log.e(TAG, "Seek failed", e)
        }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        try {
            exoPlayer?.setPlaybackSpeed(speed)
            _playbackSpeed.value = speed
        } catch (e: Exception) {
            Log.e(TAG, "Set speed failed", e)
        }
    }
    
    fun release() {
        try {
            Log.d(TAG, "Releasing player")
            exoPlayer?.release()
            exoPlayer = null
            currentUrl = null
            _error.value = null
            _isPlaying.value = false
            _buffering.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Release failed", e)
        }
    }
    
    fun releasePlayer() {
        release()
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
                    Log.d(TAG, "Buffering...")
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
            _error.value = when {
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> 
                    "Network connection failed. Check your internet."
                error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                    "Stream not found. URL may be invalid."
                error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                    "Video format not supported on this device."
                else -> error.message ?: "Playback error. Try again."
            }
            _buffering.value = false
        }
        
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            Log.d(TAG, "Video size changed: ${width}x$height")
        }
        
        override fun onRenderedFirstFrame() {
            Log.d(TAG, "First frame rendered")
            _buffering.value = false
        }
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
}
