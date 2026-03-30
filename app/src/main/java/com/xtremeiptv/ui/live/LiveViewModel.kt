package com.xtremeiptv.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.protocol.*
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val xtreamClient: XtreamClient,
    private val stalkerClient: StalkerClient,
    private val macClient: MacClient,
    private val m3uLoader: M3uLoader
) : ViewModel() {
    
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadChannels()
    }
    
    private fun loadChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val profile = profileRepository.getActiveProfile().firstOrNull()
                if (profile == null) {
                    _error.value = "No active profile"
                    _isLoading.value = false
                    return@launch
                }
                
                val result = when (profile.protocolType) {
                    "xtream" -> {
                        val creds = XtreamClient.Credentials(profile.serverUrl, profile.username!!, profile.password!!)
                        xtreamClient.getLiveChannels(creds)
                    }
                    "m3u" -> {
                        val m3uResult = if (profile.serverUrl.startsWith("http")) {
                            m3uLoader.loadFromUrl(profile.serverUrl)
                        } else {
                            m3uLoader.loadFromFile(profile.serverUrl)
                        }
                        m3uResult.channels
                    }
                    "stalker" -> {
                        val creds = StalkerClient.StalkerCredentials(
                            profile.serverUrl,
                            profile.username!!,
                            profile.password!!,
                            profile.macAddress!!
                        )
                        val token = stalkerClient.authenticate(creds)
                        if (token != null) {
                            stalkerClient.getLiveChannels(profile.serverUrl, token)
                        } else {
                            _error.value = "Stalker auth failed"
                            emptyList()
                        }
                    }
                    "mac" -> {
                        val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress!!)
                        val token = macClient.authenticate(creds)
                        if (token != null) {
                            macClient.getLiveChannels(profile.serverUrl, token)
                        } else {
                            _error.value = "MAC auth failed"
                            emptyList()
                        }
                    }
                    else -> emptyList()
                }
                
                _channels.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadChannels()
    }
}