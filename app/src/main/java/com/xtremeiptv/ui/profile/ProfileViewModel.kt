package com.xtremeiptv.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.database.entity.Profile
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    val profiles: StateFlow<List<Profile>> = profileRepository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
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
    
    fun saveProfile(
        id: String?,
        name: String,
        protocolType: String,
        serverUrl: String,
        username: String?,
        password: String?,
        macAddress: String?,
        onResult: (Boolean) -> Unit
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
            
            try {
                if (id == null) {
                    profileRepository.addProfile(profile)
                } else {
                    profileRepository.updateProfile(profile)
                }
                onResult(true)
            } catch (e: Exception) {
                _error.value = e.message
                onResult(false)
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
        }
    }
}