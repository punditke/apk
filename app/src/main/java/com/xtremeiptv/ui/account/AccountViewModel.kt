package com.xtremeiptv.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtremeiptv.data.network.protocol.*
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val xtreamClient: XtreamClient,
    private val stalkerClient: StalkerClient,
    private val macClient: MacClient
) : ViewModel() {
    
    data class AccountInfo(
        val portalUrl: String,
        val serverIp: String,
        val protocol: String,
        val status: String,
        val macAddress: String? = null,
        val username: String? = null,
        val createdDate: String? = null,
        val expiryDate: String? = null,
        val remainingDays: String? = null,
        val tariffPlan: String? = null,
        val maxConnections: Int? = null
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
                val serverIp = profile.serverUrl
                    .replace("http://", "")
                    .replace("https://", "")
                    .split("/")[0]
                
                // Fetch real account data based on protocol
                val accountData = when (profile.protocolType) {
                    "xtream" -> {
                        val creds = XtreamClient.Credentials(profile.serverUrl, profile.username ?: "", profile.password ?: "")
                        xtreamClient.getAccountInfo(creds)
                    }
                    "stalker" -> {
                        val creds = StalkerClient.StalkerCredentials(
                            profile.serverUrl,
                            profile.username ?: "",
                            profile.password ?: "",
                            profile.macAddress ?: ""
                        )
                        stalkerClient.getAccountInfo(creds)
                    }
                    "mac" -> {
                        val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress ?: "")
                        macClient.getAccountInfo(creds)
                    }
                    else -> null
                }
                
                // Extract values safely
                val createdDate = accountData?.let { it.createdDate }
                val expiryDate = accountData?.let { it.expiryDate }
                val tariffPlan = accountData?.let { it.tariffPlan }
                val maxConnections = accountData?.let { it.maxConnections }
                
                _accountInfo.value = AccountInfo(
                    portalUrl = profile.serverUrl,
                    serverIp = serverIp,
                    protocol = profile.protocolType,
                    status = if (accountData != null) "Connected" else "Error",
                    macAddress = profile.macAddress,
                    username = profile.username,
                    createdDate = createdDate,
                    expiryDate = expiryDate,
                    remainingDays = if (expiryDate != null) calculateRemainingDays(expiryDate) else null,
                    tariffPlan = tariffPlan,
                    maxConnections = maxConnections
                )
            }
            
            _isLoading.value = false
        }
    }
    
    private fun calculateRemainingDays(expiryDateStr: String): String? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expiry = format.parse(expiryDateStr)
            val now = Date()
            if (expiry != null && expiry.after(now)) {
                val diff = expiry.time - now.time
                val days = diff / (1000 * 60 * 60 * 24)
                "$days days"
            } else {
                "Expired"
            }
        } catch (e: Exception) {
            null
        }
    }
}