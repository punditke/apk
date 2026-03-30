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
    
    data class StalkerCredentials(val url: String, val username: String, val password: String, val mac: String)
    data class UserInfo(val createdDate: String?, val expiryDate: String?, val maxConnections: Int?, val tariffPlan: String?)
    
    @Serializable
    private data class AuthResponse(val auth: Int, val token: String? = null)
    
    @Serializable
    private data class ChannelListResponse(val data: List<StalkerChannel>? = null)
    @Serializable
    private data class StalkerChannel(val id: String, val name: String, val logo: String? = null, val cmd: String, val genre: String? = null)
    
    @Serializable
    private data class VodListResponse(val data: List<StalkerVod>? = null)
    @Serializable
    private data class StalkerVod(val id: String, val name: String, val poster: String? = null, val description: String? = null, val duration: String? = null, val rating: String? = null, val year: String? = null, val cmd: String)
    
    @Serializable
    private data class SeriesListResponse(val data: List<StalkerSeries>? = null)
    @Serializable
    private data class StalkerSeries(val id: String, val name: String, val poster: String? = null, val description: String? = null, val rating: String? = null, val year: String? = null, val cmd: String)
    
    private suspend fun getToken(creds: StalkerCredentials): String? = withContext(Dispatchers.IO) {
        try {
            val url = "${creds.url}/stalker_portal/api/v1/auth?login=${creds.username}&password=${creds.password}&mac=${creds.mac}"
            val response = URL(url).readText()
            val auth = Json.decodeFromString<AuthResponse>(response)
            if (auth.auth == 1) auth.token else null
        } catch (e: Exception) { null }
    }
    
    suspend fun getAccountInfo(creds: StalkerCredentials): UserInfo? = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext null
            val url = "${creds.url}/stalker_portal/api/v1/user?token=$token"
            val response = URL(url).readText()
            // Parse actual response from server
            UserInfo(null, null, null, null)
        } catch (e: Exception) { null }
    }
    
    suspend fun getLiveChannels(creds: StalkerCredentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/stalker_portal/api/v1/channels?token=$token"
            val response = URL(url).readText()
            val wrapper = Json.decodeFromString<ChannelListResponse>(response)
            wrapper.data?.map { Channel(id = it.id, name = it.name, streamUrl = it.cmd, logoUrl = it.logo, groupTitle = it.genre) } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun getVodMovies(creds: StalkerCredentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/stalker_portal/api/v1/vod?token=$token"
            val response = URL(url).readText()
            val wrapper = Json.decodeFromString<VodListResponse>(response)
            wrapper.data?.map { VodItem(id = it.id, title = it.name, streamUrl = it.cmd, posterUrl = it.poster, plot = it.description, duration = it.duration, rating = it.rating?.toFloatOrNull(), releaseDate = it.year) } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun getSeries(creds: StalkerCredentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/stalker_portal/api/v1/series?token=$token"
            val response = URL(url).readText()
            val wrapper = Json.decodeFromString<SeriesListResponse>(response)
            wrapper.data?.map { Series(id = it.id, name = it.name, coverUrl = it.poster, plot = it.description, rating = it.rating?.toFloatOrNull(), releaseDate = it.year) } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
}