package com.xtremeiptv.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
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
        fun newIntent(context: Context, contentId: String, contentType: String, title: String, streamUrl: String): Intent {
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
        
        val streamUrl = intent.getStringExtra("stream_url") ?: ""
        
        if (streamUrl.isEmpty()) {
            finish()
            return
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            XtremeIPTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlayerScreen(
                        streamUrl = streamUrl,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            return true
        }
        return super.onKeyDown(keyCode, event)
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
    
    // Load stream when screen appears
    LaunchedEffect(streamUrl) {
        viewModel.loadStream(streamUrl)
    }
    
    // Clean up when screen disappears
    DisposableEffect(Unit) {
        onDispose {
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
                    keepScreenOn = true
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
            onPlayPause = { if (isPlaying) viewModel.pause() else viewModel.play() },
            onSeek = { viewModel.seekTo(it) },
            onSpeedChange = { viewModel.setPlaybackSpeed(it) },
            onSkipForward = { viewModel.seekTo(currentPosition + 10000) },
            onSkipBackward = { viewModel.seekTo(currentPosition - 10000) },
            onBack = onBack
        )
        
        // Loading Indicator
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
        
        // Error Dialog
        if (error != null && !isPlaying && !isLoading) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { onBack() },
                title = { androidx.compose.material3.Text("Playback Error") },
                text = { androidx.compose.material3.Text(error!!) },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = { onBack() }) {
                        androidx.compose.material3.Text("OK")
                    }
                }
            )
        }
    }
}
