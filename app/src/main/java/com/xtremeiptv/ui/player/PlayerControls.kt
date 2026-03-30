package com.xtremeiptv.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSkipBackward) {
                Icon(Icons.Default.FastRewind, contentDescription = "-10s")
            }
            
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            
            IconButton(onClick = onSkipForward) {
                Icon(Icons.Default.FastForward, contentDescription = "+10s")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Speed:", style = MaterialTheme.typography.bodySmall)
            SpeedButton(speed = 0.5f, currentSpeed = playbackSpeed, onClick = onSpeedChange)
            SpeedButton(speed = 1.0f, currentSpeed = playbackSpeed, onClick = onSpeedChange)
            SpeedButton(speed = 1.5f, currentSpeed = playbackSpeed, onClick = onSpeedChange)
            SpeedButton(speed = 2.0f, currentSpeed = playbackSpeed, onClick = onSpeedChange)
        }
    }
}

@Composable
fun SpeedButton(speed: Float, currentSpeed: Float, onClick: (Float) -> Unit) {
    Button(
        onClick = { onClick(speed) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (currentSpeed == speed) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.size(48.dp, 36.dp)
    ) {
        Text("${speed}x", style = MaterialTheme.typography.labelSmall)
    }
}
