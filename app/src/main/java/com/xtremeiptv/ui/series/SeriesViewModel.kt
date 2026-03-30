package com.xtremeiptv.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.protocol.*
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val xtreamClient: XtreamClient,
    private val stalkerClient: StalkerClient,
    private val macClient: MacClient,
    private val m3uLoader: M3uLoader
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
    
    fun loadSeries() {
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
                        val creds = XtreamClient.Credentials(profile.serverUrl, profile.username ?: "", profile.password ?: "")
                        xtreamClient.getSeries(creds)
                    }
                    "stalker" -> {
                        val creds = StalkerClient.StalkerCredentials(
                            profile.serverUrl, profile.username ?: "", profile.password ?: "", profile.macAddress ?: ""
                        )
                        stalkerClient.getSeries(creds)
                    }
                    "mac" -> {
                        val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress ?: "")
                        macClient.getSeries(creds)
                    }
                    "m3u" -> {
                        val m3uResult = if (profile.serverUrl.startsWith("http")) {
                            m3uLoader.loadFromUrl(profile.serverUrl)
                        } else {
                            m3uLoader.loadFromFile(profile.serverUrl)
                        }
                        m3uResult.series
                    }
                    else -> emptyList()
                }
                
                _series.value = result
                if (result.isEmpty() && _error.value == null) {
                    _error.value = "No series found"
                }
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
