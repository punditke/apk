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
class XtreamClient @Inject constructor() {
    
    data class Credentials(val url: String, val username: String, val password: String)
    
    @Serializable
    data class UserInfo(
        val username: String,
        val password: String,
        val message: String? = null,
        val auth: Int = 0,
        val status: String? = null,
        val exp_date: String? = null,
        val is_trial: String? = null,
        val active_cons: String? = null,
        val max_connections: String? = null,
        val created_at: String? = null,
        val tariff_plan: String? = null
    )
    
    @Serializable
    private data class LiveStream(
        val stream_id: String,
        val num: Int,
        val name: String,
        val stream_type: String,
        val stream_icon: String? = null,
        val epg_channel_id: String? = null,
        val added: String,
        val category_id: String,
        val category_name: String? = null,
        val container_extension: String = "ts"
    )
    
    @Serializable
    private data class VodStream(
        val stream_id: String,
        val num: Int,
        val name: String,
        val stream_type: String,
        val stream_icon: String? = null,
        val added: String,
        val category_id: String,
        val category_name: String? = null,
        val container_extension: String = "mp4",
        val plot: String? = null,
        val backdrop_path: String? = null,
        val duration: String? = null,
        val rating: String? = null
    )
    
    @Serializable
    private data class SeriesInfo(
        val series_id: String,
        val num: Int,
        val name: String,
        val cover: String? = null,
        val plot: String? = null,
        val backdrop_path: String? = null,
        val rating: String? = null,
        val releaseDate: String? = null,
        val category_id: String,
        val category_name: String? = null
    )
    
    @Serializable
    private data class SeriesData(
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
        val duration: String? = null,
        val releasedate: String? = null
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private fun cleanUrl(url: String): String {
        var cleaned = url.trim()
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            cleaned = "http://$cleaned"
        }
        return cleaned
    }
    
    suspend fun getAccountInfo(creds: Credentials): UserInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "${cleanUrl(creds.url)}/player_api.php?username=${creds.username}&password=${creds.password}"
            val response = URL(url).readText()
            json.decodeFromString<UserInfo>(response)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getLiveChannels(creds: Credentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/player_api.php?username=${creds.username}&password=${creds.password}&action=get_live_streams"
            val response = URL(url).readText()
            val streams = json.decodeFromString<List<LiveStream>>(response)
            streams.map {
                Channel(
                    id = it.stream_id,
                    name = it.name,
                    streamUrl = "$baseUrl/live/${creds.username}/${creds.password}/${it.stream_id}.${it.container_extension}",
                    logoUrl = it.stream_icon,
                    groupTitle = it.category_name,
                    epgId = it.epg_channel_id
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getVodMovies(creds: Credentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/player_api.php?username=${creds.username}&password=${creds.password}&action=get_vod_streams"
            val response = URL(url).readText()
            val streams = json.decodeFromString<List<VodStream>>(response)
            streams.map {
                VodItem(
                    id = it.stream_id,
                    title = it.name,
                    streamUrl = "$baseUrl/movie/${creds.username}/${creds.password}/${it.stream_id}.${it.container_extension}",
                    posterUrl = it.stream_icon,
                    backdropUrl = it.backdrop_path,
                    plot = it.plot,
                    duration = it.duration,
                    rating = it.rating?.toFloatOrNull(),
                    releaseDate = it.added
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getSeriesList(creds: Credentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/player_api.php?username=${creds.username}&password=${creds.password}&action=get_series"
            val response = URL(url).readText()
            val seriesList = json.decodeFromString<List<SeriesInfo>>(response)
            seriesList.map {
                Series(
                    id = it.series_id,
                    name = it.name,
                    coverUrl = it.cover,
                    backdropUrl = it.backdrop_path,
                    plot = it.plot,
                    rating = it.rating?.toFloatOrNull(),
                    releaseDate = it.releaseDate,
                    seasons = emptyList()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getSeriesInfo(creds: Credentials, seriesId: String): List<Season> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = cleanUrl(creds.url)
            val url = "$baseUrl/player_api.php?username=${creds.username}&password=${creds.password}&action=get_series_info&series_id=$seriesId"
            val response = URL(url).readText()
            val seriesData = json.decodeFromString<SeriesData>(response)
            
            seriesData.seasons?.map { seasonData ->
                Season(
                    seasonNumber = seasonData.season_number,
                    episodes = seasonData.episodes?.map { episodeData ->
                        Episode(
                            id = episodeData.id,
                            title = episodeData.title,
                            streamUrl = "$baseUrl/series/${creds.username}/${creds.password}/${episodeData.id}.${episodeData.container_extension}",
                            episodeNumber = episodeData.episode_num.toIntOrNull() ?: 0,
                            seasonNumber = seasonData.season_number,
                            plot = episodeData.info?.plot,
                            duration = episodeData.info?.duration,
                            thumbnailUrl = null,
                            releaseDate = episodeData.info?.releasedate
                        )
                    } ?: emptyList()
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
