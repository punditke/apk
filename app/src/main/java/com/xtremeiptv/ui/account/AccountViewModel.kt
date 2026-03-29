package com.xtremeiptv.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    data class AccountInfo(
        val serverUrl: String,
        val username: String?,
        val macAddress: String?,
        val protocol: String,
        val status: String,
        val expiry: String?
    )
    
    private val _accountInfo = MutableStateFlow<AccountInfo?>(null)
    val accountInfo: StateFlow<AccountInfo?> = _accountInfo.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadAccountInfo()
    }
    
    private fun loadAccountInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val profile = profileRepository.getActiveProfile().firstOrNull()
            
            if (profile != null) {
                _accountInfo.value = AccountInfo(
                    serverUrl = profile.serverUrl,
                    username = profile.username,
                    macAddress = profile.macAddress,
                    protocol = profile.protocolType,
                    status = "Connected",
                    expiry = null // Would fetch from server
                )
            }
            
            _isLoading.value = false
        }
    }
}
