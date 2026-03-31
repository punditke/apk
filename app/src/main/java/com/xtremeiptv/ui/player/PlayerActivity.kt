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
import androidx.compose.material3.*
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
    
    override fun onDestroy() {
        super.onDestroy()
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
    
    // Load stream only once
    LaunchedEffect(streamUrl) {
        viewModel.loadStream(streamUrl)
    }
    
    // Clean up on dispose
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
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
        
        // Error Dialog - only show if not playing
        if (error != null && !isPlaying && !isLoading) {
            AlertDialog(
                onDismissRequest = { onBack() },
                title = { Text("Playback Error") },
                text = { Text(error!!) },
                confirmButton = {
                    Button(onClick = { onBack() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
