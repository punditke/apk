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
        val token: String? = null
    )
    
    @Serializable
    private data class ChannelListResponse(
        val data: List<MacChannel>? = null
    )
    
    @Serializable
    private data class MacChannel(
        val id: String, 
        val name: String, 
        val logo: String? = null, 
        val stream_url: String, 
        val category: String? = null
    )
    
    @Serializable
    private data class VodListResponse(
        val data: List<MacVod>? = null
    )
    
    @Serializable
    private data class MacVod(
        val id: String, 
        val name: String, 
        val poster: String? = null, 
        val description: String? = null, 
        val duration: String? = null, 
        val rating: String? = null, 
        val year: String? = null, 
        val stream_url: String
    )
    
    @Serializable
    private data class SeriesListResponse(
        val data: List<MacSeries>? = null
    )
    
    @Serializable
    private data class MacSeries(
        val id: String, 
        val name: String, 
        val poster: String? = null, 
        val description: String? = null, 
        val rating: String? = null, 
        val year: String? = null, 
        val stream_url: String
    )
    
    private suspend fun getToken(creds: MacCredentials): String? = withContext(Dispatchers.IO) {
        try {
            val url = "${creds.url}/c/?mac=${creds.mac}&type=stb"
            val response = URL(url).readText()
            val auth = Json.decodeFromString<AuthResponse>(response)
            auth.token
        } catch (e: Exception) { 
            null 
        }
    }
    
    suspend fun getAccountInfo(creds: MacCredentials): UserInfo? = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext null
            val url = "${creds.url}/c/get_user?token=$token"
            val response = URL(url).readText()
            // Parse actual response from server
            // For now return null until API format is known
            UserInfo(null, null, null, null)
        } catch (e: Exception) { 
            null 
        }
    }
    
    suspend fun getLiveChannels(creds: MacCredentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/c/get_channels?token=$token"
            val response = URL(url).readText()
            val wrapper = Json.decodeFromString<ChannelListResponse>(response)
            wrapper.data?.map { 
                Channel(
                    id = it.id, 
                    name = it.name, 
                    streamUrl = it.stream_url, 
                    logoUrl = it.logo, 
                    groupTitle = it.category
                ) 
            } ?: emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getVodMovies(creds: MacCredentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/c/get_vod?token=$token"
            val response = URL(url).readText()
            val wrapper = Json.decodeFromString<VodListResponse>(response)
            wrapper.data?.map { 
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
            } ?: emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getSeries(creds: MacCredentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/c/get_series?token=$token"
            val response = URL(url).readText()
            val wrapper = Json.decodeFromString<SeriesListResponse>(response)
            wrapper.data?.map { 
                Series(
                    id = it.id, 
                    name = it.name, 
                    coverUrl = it.poster, 
                    plot = it.description, 
                    rating = it.rating?.toFloatOrNull(), 
                    releaseDate = it.year
                ) 
            } ?: emptyList()
        } catch (e: Exception) { 
            emptyList() 
        }
    }
}