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
        val name: String? = null,
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
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadAccountInfo()
    }
    
    private fun loadAccountInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val profile = profileRepository.getActiveProfile().firstOrNull()
            
            if (profile != null) {
                val serverIp = profile.serverUrl
                    .replace("http://", "")
                    .replace("https://", "")
                    .split("/")[0]
                
                var name: String? = null
                var createdDate: String? = null
                var expiryDate: String? = null
                var maxConnections: Int? = null
                var tariffPlan: String? = null
                
                try {
                    when (profile.protocolType) {
                        "xtream" -> {
                            val creds = XtreamClient.Credentials(profile.serverUrl, profile.username ?: "", profile.password ?: "")
                            val info = xtreamClient.getAccountInfo(creds)
                            name = info?.username
                            createdDate = info?.created_at
                            expiryDate = info?.exp_date?.let { 
                                try {
                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                        .format(Date(it.toLong() * 1000))
                                } catch (e: Exception) { null }
                            }
                            maxConnections = info?.max_connections?.toIntOrNull()
                            tariffPlan = info?.tariff_plan
                        }
                        "stalker" -> {
                            val creds = StalkerClient.StalkerCredentials(
                                profile.serverUrl, profile.username ?: "", profile.password ?: "", profile.macAddress ?: ""
                            )
                            val info = stalkerClient.getAccountInfo(creds)
                            name = info?.name
                            expiryDate = info?.expiryDate
                            maxConnections = info?.maxConnections
                            tariffPlan = info?.tariffPlan
                        }
                        "mac" -> {
                            val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress ?: "")
                            val info = macClient.getAccountInfo(creds)
                            expiryDate = info?.expiryDate
                            maxConnections = info?.maxConnections
                        }
                    }
                } catch (e: Exception) {
                    _error.value = "Failed to fetch account info: ${e.message}"
                }
                
                val remainingDays = if (expiryDate != null) calculateRemainingDays(expiryDate) else null
                
                _accountInfo.value = AccountInfo(
                    portalUrl = profile.serverUrl,
                    serverIp = serverIp,
                    protocol = profile.protocolType,
                    status = if (expiryDate != null) "Active" else "Connected",
                    macAddress = profile.macAddress,
                    username = profile.username,
                    name = name,
                    createdDate = createdDate,
                    expiryDate = expiryDate,
                    remainingDays = remainingDays,
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
    
    fun refresh() {
        loadAccountInfo()
    }
}
