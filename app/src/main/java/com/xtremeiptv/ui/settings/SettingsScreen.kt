package com.xtremeiptv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isBackgroundPlayEnabled by viewModel.isBackgroundPlayEnabled.collectAsState()
    val isPipEnabled by viewModel.isPipEnabled.collectAsState()
    val defaultPlaybackSpeed by viewModel.defaultPlaybackSpeed.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Playback", style = MaterialTheme.typography.titleMedium)
            }
            
            item {
                SettingsSwitchItem(
                    title = "Background Playback",
                    checked = isBackgroundPlayEnabled,
                    onCheckedChange = { viewModel.toggleBackgroundPlay() }
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Picture-in-Picture",
                    checked = isPipEnabled,
                    onCheckedChange = { viewModel.togglePip() }
                )
            }
            
            item {
                SettingsSliderItem(
                    title = "Default Playback Speed",
                    value = defaultPlaybackSpeed,
                    onValueChange = { viewModel.setDefaultPlaybackSpeed(it) },
                    valueRange = 0.5f..2.0f
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Data", style = MaterialTheme.typography.titleMedium)
            }
            
            item {
                Button(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear Cache")
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsSliderItem(title: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${String.format("%.1f", value)}x", style = MaterialTheme.typography.bodySmall)
        }
    }
}
