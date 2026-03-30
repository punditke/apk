package com.xtremeiptv.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.protocol.XtreamClient
import com.xtremeiptv.data.repository.ContentRepository
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val contentRepository: ContentRepository,
    private val xtreamClient: XtreamClient
) : ViewModel() {
    
    private val _series = MutableStateFlow<List<Series>>(emptyList())
    val series: StateFlow<List<Series>> = _series.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadSeries()
    }
    
    private fun loadSeries() {
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
                        xtreamClient.getSeries(creds)
                    }
                    "m3u" -> {
                        // Load from M3U - implement
                        emptyList()
                    }
                    "stalker" -> {
                        // Stalker implementation
                        emptyList()
                    }
                    "mac" -> {
                        // MAC implementation
                        emptyList()
                    }
                    else -> emptyList()
                }
                
                _series.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        loadSeries()
    }
}