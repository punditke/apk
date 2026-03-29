package com.xtremeiptv.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.local.EncryptedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {
    
    private val _isBackgroundPlayEnabled = MutableStateFlow(false)
    val isBackgroundPlayEnabled: StateFlow<Boolean> = _isBackgroundPlayEnabled.asStateFlow()
    
    private val _isPipEnabled = MutableStateFlow(false)
    val isPipEnabled: StateFlow<Boolean> = _isPipEnabled.asStateFlow()
    
    private val _defaultPlaybackSpeed = MutableStateFlow(1.0f)
    val defaultPlaybackSpeed: StateFlow<Float> = _defaultPlaybackSpeed.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        _isBackgroundPlayEnabled.value = encryptedPrefs.getBoolean("background_play", false)
        _isPipEnabled.value = encryptedPrefs.getBoolean("pip_enabled", false)
        _defaultPlaybackSpeed.value = encryptedPrefs.getLong("playback_speed", 100) / 100f
    }
    
    fun toggleBackgroundPlay() {
        val newValue = !_isBackgroundPlayEnabled.value
        _isBackgroundPlayEnabled.value = newValue
        encryptedPrefs.putBoolean("background_play", newValue)
    }
    
    fun togglePip() {
        val newValue = !_isPipEnabled.value
        _isPipEnabled.value = newValue
        encryptedPrefs.putBoolean("pip_enabled", newValue)
    }
    
    fun setDefaultPlaybackSpeed(speed: Float) {
        _defaultPlaybackSpeed.value = speed
        encryptedPrefs.putLong("playback_speed", (speed * 100).toLong())
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // Implement cache clearing logic
        }
    }
}
