package com.xtremeiptv.data.network.protocol

import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Episode
import com.xtremeiptv.data.network.model.Season
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
        val createdDate: String?,
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
    private data class ChannelData(
        val id: String,
        val name: String,
        val logo: String? = null,
        val cmd: String,
        val genre: String? = null,
        val number: Int? = null
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
        val cmd: String
    )
    
    @Serializable
    private data class SeriesData(
        val id: String,
        val name: String,
        val poster: String? = null,
        val description: String? = null,
        val rating: String? = null,
        val year: String? = null,
        val cmd: String,
        val seasons: List<SeasonData>? = null
    )
    
    @Serializable
    private data class SeasonData(
        val season_number: Int,
        val episodes: List<EpisodeData>? = null
    )
    
    @Serializable
    private data class EpisodeData(
        val id: String,
        val episode_num: String,
        val title: String,
        val container_extension: String,
        val info: EpisodeInfo?
    )
    
    @Serializable
    private data class EpisodeInfo(
        val plot: String? = null,
        val duration: String? = null
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private suspend fun getToken(creds: StalkerCredentials): String? = withContext(Dispatchers.IO) {
        try {
            val url = "${creds.url}/stalker_portal/api/v1/auth?login=${creds.username}&password=${creds.password}&mac=${creds.mac}"
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
            val url = "${creds.url}/stalker_portal/api/v1/user?token=$token"
            val response = URL(url).readText()
            UserInfo(null, null, null, null)
        } catch (e: Exception) { null }
    }
    
    suspend fun getLiveChannels(creds: StalkerCredentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/stalker_portal/api/v1/channels?token=$token"
            val response = URL(url).readText()
            val channels = json.decodeFromString<List<ChannelData>>(response)
            channels.map {
                Channel(
                    id = it.id,
                    name = it.name,
                    streamUrl = it.cmd,
                    logoUrl = it.logo,
                    groupTitle = it.genre
                )
            }
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getVodMovies(creds: StalkerCredentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/stalker_portal/api/v1/vod?token=$token"
            val response = URL(url).readText()
            val vods = json.decodeFromString<List<VodData>>(response)
            vods.map {
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
            }
        } catch (e: Exception) { 
            emptyList() 
        }
    }
    
    suspend fun getSeries(creds: StalkerCredentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val token = getToken(creds) ?: return@withContext emptyList()
            val url = "${creds.url}/stalker_portal/api/v1/series?token=$token"
            val response = URL(url).readText()
            val seriesList = json.decodeFromString<List<SeriesData>>(response)
            seriesList.map { series ->
                val seasons = series.seasons?.mapNotNull { seasonData ->
                    val episodes = seasonData.episodes?.map { episodeData ->
                        Episode(
                            id = episodeData.id,
                            title = episodeData.title,
                            streamUrl = "${creds.url}/series/${creds.username}/${creds.password}/${episodeData.id}.${episodeData.container_extension}",
                            episodeNumber = episodeData.episode_num.toIntOrNull() ?: 0,
                            seasonNumber = seasonData.season_number,
                            plot = episodeData.info?.plot,
                            duration = episodeData.info?.duration,
                            thumbnailUrl = null
                        )
                    } ?: emptyList()
                    
                    if (episodes.isNotEmpty()) {
                        Season(
                            seasonNumber = seasonData.season_number,
                            episodes = episodes
                        )
                    } else null
                } ?: emptyList()
                
                Series(
                    id = series.id,
                    name = series.name,
                    coverUrl = series.poster,
                    plot = series.description,
                    rating = series.rating?.toFloatOrNull(),
                    releaseDate = series.year,
                    seasons = seasons
                )
            }
        } catch (e: Exception) { 
            emptyList() 
        }
    }
}
