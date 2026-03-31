package com.xtremeiptv.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.database.entity.Profile
import com.xtremeiptv.data.network.protocol.*
import com.xtremeiptv.data.repository.ContentRepository
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val contentRepository: ContentRepository,
    private val xtreamClient: XtreamClient,
    private val stalkerClient: StalkerClient,
    private val macClient: MacClient,
    private val m3uLoader: M3uLoader
) : ViewModel() {
    
    data class ProfileValidationResult(
        val success: Boolean,
        val message: String,
        val channels: Int = 0,
        val movies: Int = 0,
        val series: Int = 0
    )
    
    val profiles: StateFlow<List<Profile>> = profileRepository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _validationResult = MutableStateFlow<ProfileValidationResult?>(null)
    val validationResult: StateFlow<ProfileValidationResult?> = _validationResult.asStateFlow()
    
    fun getProfile(profileId: String?): Flow<Profile?> {
        return if (profileId == null) {
            flowOf(null)
        } else {
            profileRepository.getAllProfiles().map { profiles ->
                profiles.find { it.id == profileId }
            }
        }
    }
    
    fun setActiveProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.setActiveProfile(profileId)
        }
    }
    
    suspend fun validateAndGetStats(profile: Profile): ProfileValidationResult {
        return try {
            val channels = contentRepository.loadLiveChannels(profile, useCache = false)
            val movies = contentRepository.loadMovies(profile, useCache = false)
            val series = contentRepository.loadSeries(profile, useCache = false)
            
            ProfileValidationResult(
                success = channels.isNotEmpty() || movies.isNotEmpty() || series.isNotEmpty(),
                message = "Loaded: ${channels.size} channels, ${movies.size} movies, ${series.size} series",
                channels = channels.size,
                movies = movies.size,
                series = series.size
            )
        } catch (e: Exception) {
            ProfileValidationResult(false, "Error: ${e.message}")
        }
    }
    
    fun saveProfile(
        id: String?,
        name: String,
        protocolType: String,
        serverUrl: String,
        username: String?,
        password: String?,
        macAddress: String?,
        onResult: (Boolean, String, Int, Int, Int) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val profile = Profile(
                id = id ?: UUID.randomUUID().toString(),
                name = name,
                protocolType = protocolType,
                serverUrl = serverUrl,
                username = username,
                password = password,
                macAddress = macAddress
            )
            
            val validation = validateAndGetStats(profile)
            _validationResult.value = validation
            
            if (validation.success) {
                try {
                    if (id == null) {
                        profileRepository.addProfile(profile)
                    } else {
                        profileRepository.updateProfile(profile)
                    }
                    // Refresh cache after successful save
                    contentRepository.refreshCache(profile)
                    onResult(true, validation.message, validation.channels, validation.movies, validation.series)
                } catch (e: Exception) {
                    onResult(false, "Save failed: ${e.message}", 0, 0, 0)
                }
            } else {
                onResult(false, validation.message, 0, 0, 0)
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
            // Clear cache for deleted profile
            contentRepository.clearCache(profile.id)
        }
    }
}
