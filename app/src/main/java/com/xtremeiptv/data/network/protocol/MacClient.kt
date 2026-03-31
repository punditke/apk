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
class MacClient @Inject constructor() {
    
    data class MacCredentials(
        val url: String,
        val mac: String
    )
    
    data class UserInfo(
        val createdDate: String?,
        val expiryDate: String?,
        val maxConnections: Int?,
        val tariffPlan: String?
    )
    
    @Serializable
    private data class AuthResponse(
        val status: String? = null,
        val token: String? = null,
        val message: String? = null
    )
    
    @Serializable
    private data class ChannelData(
        val id: String,
        val name: String,
        val logo: String? = null,
        val stream_url: String,
        val category: String? = null
    )
    
    @Serializable
    private data class VodData(
        val id: String,
        val name: String,
        val poster: String? = null,
        val description: String? = null,
        val duration: String? = null,
        val rating: String? = null,
        val year: String? = null,
        val stream_url: String
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private fun cleanUrl(url: String): String {
        var cleaned = url.trim()
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned
        }
        if (cleaned.startsWith("//")) {
            cleaned = "https:$cleaned"
        }
        return cleaned
    }
    
    private suspend fun getToken(creds: MacCredentials): String? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/c/?mac=${creds.mac}&type=stb"
            val response = URL(url).readText()
            val auth = json.decodeFromString<AuthResponse>(response)
            auth.token
        } catch (e: Exception) { 
            null 
        }
    }
    
    suspend fun getAccountInfo(creds: MacCredentials): UserInfo? = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext null
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/c/get_user?token=$token"
            val response = URL(url).readText()
            // Parse user info - implementation depends on API response
            UserInfo(null, null, null, null)
        } catch (e: Exception) { 
            null 
        }
    }
    
    suspend fun getLiveChannels(creds: MacCredentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/c/get_channels?token=$token"
            val response = URL(url).readText()
            val channels = json.decodeFromString<List<ChannelData>>(response)
            channels.map {
                Channel(
                    id = it.id,
                    name = it.name,
                    streamUrl = it.stream_url,
                    logoUrl = it.logo,
                    groupTitle = it.category
                )
            }
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getVodMovies(creds: MacCredentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/c/get_vod?token=$token"
            val response = URL(url).readText()
            val vods = json.decodeFromString<List<VodData>>(response)
            vods.map {
                VodItem(
                    id = it.id,
                    title = it.name,
                    streamUrl = it.stream_url,
                    posterUrl = it.poster,
                    plot = it.description,
                    duration = it.duration,
                    rating = it.rating?.toFloatOrNull(),
                    releaseDate = it.year
                )
            }
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getSeries(creds: MacCredentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/c/get_series?token=$token"
            val response = URL(url).readText()
            // For now, return empty list
            emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
}
