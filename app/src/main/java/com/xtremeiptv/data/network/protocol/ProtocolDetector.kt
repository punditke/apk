package com.xtremeiptv.data.network.protocol

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtocolDetector @Inject constructor() {
    
    sealed class DetectionResult {
        data class Xtream(val url: String, val username: String, val password: String) : DetectionResult()
        data class Stalker(val url: String, val username: String, val password: String, val mac: String) : DetectionResult()
        data class Mac(val url: String, val mac: String) : DetectionResult()
        data class M3u(val url: String?) : DetectionResult() // null means local file
        object Unknown : DetectionResult()
    }
    
    fun detect(
        serverUrl: String? = null,
        username: String? = null,
        password: String? = null,
        macAddress: String? = null,
        m3uContent: String? = null
    ): DetectionResult {
        return when {
            // Xtream: has username, password, and server URL
            !username.isNullOrEmpty() && !password.isNullOrEmpty() && !serverUrl.isNullOrEmpty() && macAddress.isNullOrEmpty() -> {
                DetectionResult.Xtream(serverUrl, username, password)
            }
            // Stalker: has username, password, mac, and server URL
            !username.isNullOrEmpty() && !password.isNullOrEmpty() && !macAddress.isNullOrEmpty() && !serverUrl.isNullOrEmpty() -> {
                DetectionResult.Stalker(serverUrl, username, password, macAddress)
            }
            // MAC Portal: has mac and server URL only
            !macAddress.isNullOrEmpty() && !serverUrl.isNullOrEmpty() && username.isNullOrEmpty() -> {
                DetectionResult.Mac(serverUrl, macAddress)
            }
            // M3U: has content or URL
            m3uContent != null || !serverUrl.isNullOrEmpty() -> {
                DetectionResult.M3u(serverUrl)
            }
            else -> DetectionResult.Unknown
        }
    }
}
