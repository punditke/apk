package com.xtremeiptv.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.xtremeiptv.utils.XtremeIPTVTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "PlayerActivity"
        
        fun newIntent(context: Context, contentId: String, contentType: String, title: String, streamUrl: String): Intent {
            Log.d(TAG, "newIntent: contentId=$contentId, contentType=$contentType, title=$title, streamUrl=$streamUrl")
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra("content_id", contentId)
                putExtra("content_type", contentType)
                putExtra("title", title)
                putExtra("stream_url", streamUrl)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        val streamUrl = intent.getStringExtra("stream_url") ?: ""
        
        if (streamUrl.isEmpty()) {
            Log.e(TAG, "No stream URL provided, finishing")
            finish()
            return
        }
        
        Log.d(TAG, "Stream URL: $streamUrl")
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            XtremeIPTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlayerScreen(
                        streamUrl = streamUrl,
                        onBack = { 
                            Log.d(TAG, "onBack called")
                            finish() 
                        }
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}

@Composable
fun PlayerScreen(
    streamUrl: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        Log.d("PlayerScreen", "Loading stream: $streamUrl")
        viewModel.loadStream(streamUrl)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            Log.d("PlayerScreen", "Disposing player")
            viewModel.release()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Video Player View
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.getPlayer()
                    useController = true
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Controls Overlay
        PlayerControls(
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            playbackSpeed = playbackSpeed,
            title = "Playing",
            contentType = "video",
            onPlayPause = { 
                Log.d("PlayerScreen", "Play/Pause clicked")
                if (isPlaying) viewModel.pause() else viewModel.play() 
            },
            onSeek = { position -> 
                Log.d("PlayerScreen", "Seek to: $position")
                viewModel.seekTo(position) 
            },
            onSpeedChange = { speed -> 
                Log.d("PlayerScreen", "Speed change: $speed")
                viewModel.setPlaybackSpeed(speed) 
            },
            onSkipForward = { 
                Log.d("PlayerScreen", "Skip forward")
                viewModel.seekTo(currentPosition + 10000) 
            },
            onSkipBackward = { 
                Log.d("PlayerScreen", "Skip backward")
                viewModel.seekTo(currentPosition - 10000) 
            },
            onBack = onBack
        )
        
        // Loading Indicator
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
        
        // Error Dialog
        error?.let {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { onBack() },
                title = { androidx.compose.material3.Text("Playback Error") },
                text = { androidx.compose.material3.Text(it) },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = { onBack() }) {
                        androidx.compose.material3.Text("OK")
                    }
                }
            )
        }
    }
}
