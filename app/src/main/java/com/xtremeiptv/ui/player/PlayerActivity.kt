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
        private const val EXTRA_CONTENT_ID = "content_id"
        private const val EXTRA_CONTENT_TYPE = "content_type"
        private const val EXTRA_TITLE = "title"
        
        fun newIntent(context: Context, contentId: String, contentType: String, title: String): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_CONTENT_ID, contentId)
                putExtra(EXTRA_CONTENT_TYPE, contentType)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }
    
    private var contentId: String = ""
    private var contentType: String = ""
    private var title: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        contentId = intent.getStringExtra(EXTRA_CONTENT_ID) ?: ""
        contentType = intent.getStringExtra(EXTRA_CONTENT_TYPE) ?: "live"
        title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            XtremeIPTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlayerScreen(
                        contentId = contentId,
                        contentType = contentType,
                        title = title,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
    
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
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
    contentId: String,
    contentType: String,
    title: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val streamUrl by viewModel.streamUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadContent(contentId, contentType)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                // Loading handled by ViewModel
            }
            error != null -> {
                // Error handled by ViewModel
            }
            streamUrl != null -> {
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = viewModel.getPlayer()
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
