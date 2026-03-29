package com.xtremeiptv.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _currentProfileId = MutableStateFlow<String?>(null)
    val currentProfileId: StateFlow<String?> = _currentProfileId.asStateFlow()
    
    private val _profileName = MutableStateFlow("")
    val profileName: StateFlow<String> = _profileName.asStateFlow()
    
    init {
        loadActiveProfile()
    }
    
    private fun loadActiveProfile() {
        viewModelScope.launch {
            val profile = profileRepository.getActiveProfile().collect { profile ->
                _currentProfileId.value = profile?.id
                _profileName.value = profile?.name ?: ""
            }
        }
    }
    
    fun switchProfile() {
        // Navigation handled in MainActivity
    }
}
