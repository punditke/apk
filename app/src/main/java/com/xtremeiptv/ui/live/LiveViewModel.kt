package com.xtremeiptv.ui.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.repository.ContentRepository
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {
    
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentProfileId: String? = null
    
    init {
        loadChannels()
    }
    
    fun loadChannels(forceRefresh: Boolean = false) {
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
                
                currentProfileId = profile.id
                val result = contentRepository.loadLiveChannels(profile, useCache = !forceRefresh)
                
                _channels.value = result
                
                if (result.isEmpty() && !forceRefresh) {
                    // If cache was empty, try force refresh from network
                    loadChannels(forceRefresh = true)
                } else if (result.isEmpty()) {
                    _error.value = "No channels found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load channels"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadChannels(forceRefresh = true)
    }
    
    fun clearError() {
        _error.value = null
    }
}
