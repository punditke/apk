package com.xtremeiptv.data.network.protocol

import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StalkerClient @Inject constructor() {
    
    data class StalkerCredentials(
        val url: String,
        val username: String,
        val password: String,
        val mac: String
    )
    
    data class UserInfo(
        val name: String?,
        val login: String?,
        val expiryDate: String?,
        val maxConnections: Int?,
        val tariffPlan: String?
    )
    
    @Serializable
    private data class AuthResponse(
        val auth: Int,
        val token: String? = null,
        val message: String? = null
    )
    
    @Serializable
    private data class UserProfileResponse(
        val name: String? = null,
        val login: String? = null,
        val password: String? = null,
        val mac: String? = null,
        val expirydate: String? = null,
        val expire_billing_date: String? = null,
        val max_connections: String? = null,
        val tariff_plan: String? = null
    )
    
    @Serializable
    private data class ChannelResponse(
        val js: ChannelJs? = null
    )
    
    @Serializable
    private data class ChannelJs(
        val data: List<StalkerChannel>? = null
    )
    
    @Serializable
    private data class StalkerChannel(
        val id: String,
        val name: String,
        val logo: String? = null,
        val cmd: String,
        val tv_genre_id: String? = null
    )
    
    @Serializable
    private data class VodResponse(
        val js: VodJs? = null
    )
    
    @Serializable
    private data class VodJs(
        val data: List<StalkerVod>? = null
    )
    
    @Serializable
    private data class StalkerVod(
        val id: String,
        val name: String,
        val poster: String? = null,
        val description: String? = null,
        val duration: String? = null,
        val rating: String? = null,
        val year: String? = null,
        val cmd: String
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private fun cleanUrl(url: String): String {
        var cleaned = url.trim()
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            cleaned = "http://$cleaned"
        }
        return cleaned
    }
    
    private suspend fun getToken(creds: StalkerCredentials): String? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/stalker_portal/api/v1/auth?login=${creds.username}&password=${creds.password}&mac=${creds.mac}"
            val response = URL(url).readText()
            val auth = json.decodeFromString<AuthResponse>(response)
            if (auth.auth == 1) auth.token else null
        } catch (e: Exception) { 
            null 
        }
    }
    
    suspend fun getAccountInfo(creds: StalkerCredentials): UserInfo? = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext null
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/stalker_portal/api/v1/user?token=$token"
            val response = URL(url).readText()
            val profile = json.decodeFromString<UserProfileResponse>(response)
            UserInfo(
                name = profile.name,
                login = profile.login,
                expiryDate = profile.expirydate ?: profile.expire_billing_date,
                maxConnections = profile.max_connections?.toIntOrNull(),
                tariffPlan = profile.tariff_plan
            )
        } catch (e: Exception) { 
            null 
        }
    }
    
    suspend fun getLiveChannels(creds: StalkerCredentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/stalker_portal/api/v1/channels?token=$token"
            val response = URL(url).readText()
            val wrapper = json.decodeFromString<ChannelResponse>(response)
            wrapper.js?.data?.mapNotNull { 
                if (it.cmd.isNotBlank()) {
                    Channel(
                        id = it.id,
                        name = it.name,
                        streamUrl = it.cmd,
                        logoUrl = it.logo,
                        groupTitle = it.tv_genre_id
                    )
                } else null
            } ?: emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getVodMovies(creds: StalkerCredentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/stalker_portal/api/v1/vod?token=$token"
            val response = URL(url).readText()
            val wrapper = json.decodeFromString<VodResponse>(response)
            wrapper.js?.data?.mapNotNull {
                if (it.cmd.isNotBlank()) {
                    VodItem(
                        id = it.id,
                        title = it.name,
                        streamUrl = it.cmd,
                        posterUrl = it.poster,
                        plot = it.description,
                        duration = it.duration,
                        rating = it.rating?.toFloatOrNull(),
                        releaseDate = it.year
                    )
                } else null
            } ?: emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getSeries(creds: StalkerCredentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/stalker_portal/api/v1/series?token=$token"
            val response = URL(url).readText()
            // Parse series - implementation depends on API response structure
            emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
}
