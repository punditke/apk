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
class XtreamClient @Inject constructor() {
    
    data class Credentials(val url: String, val username: String, val password: String)
    
    @Serializable
    private data class ApiLiveStream(
        val stream_id: String,
        val name: String,
        val stream_icon: String? = null,
        val epg_channel_id: String? = null,
        val category_name: String? = null,
        val stream_type: String? = null
    )
    
    @Serializable
    private data class ApiVodStream(
        val stream_id: String,
        val name: String,
        val stream_icon: String? = null,
        val container_extension: String? = null,
        val plot: String? = null,
        val backdrop_path: String? = null,
        val duration: String? = null,
        val rating: String? = null
    )
    
    @Serializable
    private data class ApiSeries(
        val series_id: String,
        val name: String,
        val cover: String? = null,
        val plot: String? = null,
        val backdrop_path: String? = null,
        val rating: String? = null
    )
    
    suspend fun getLiveChannels(creds: Credentials): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val url = "${creds.url}/player_api.php?username=${creds.username}&password=${creds.password}&action=get_live_streams"
            val response = URL(url).readText()
            val streams = Json.decodeFromString<List<ApiLiveStream>>(response)
            streams.map {
                Channel(
                    id = it.stream_id,
                    name = it.name,
                    streamUrl = "${creds.url}/live/${creds.username}/${creds.password}/${it.stream_id}.${it.stream_type ?: "ts"}",
                    logoUrl = it.stream_icon,
                    groupTitle = it.category_name,
                    epgId = it.epg_channel_id
                )
            }
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun getVodMovies(creds: Credentials): List<VodItem> = withContext(Dispatchers.IO) {
        try {
            val url = "${creds.url}/player_api.php?username=${creds.username}&password=${creds.password}&action=get_vod_streams"
            val response = URL(url).readText()
            val streams = Json.decodeFromString<List<ApiVodStream>>(response)
            streams.map {
                VodItem(
                    id = it.stream_id,
                    title = it.name,
                    streamUrl = "${creds.url}/movie/${creds.username}/${creds.password}/${it.stream_id}.${it.container_extension ?: "mp4"}",
                    posterUrl = it.stream_icon,
                    backdropUrl = it.backdrop_path,
                    plot = it.plot,
                    duration = it.duration,
                    rating = it.rating?.toFloatOrNull()
                )
            }
        } catch (e: Exception) { emptyList() }
    }
    
    suspend fun getSeries(creds: Credentials): List<Series> = withContext(Dispatchers.IO) {
        try {
            val url = "${creds.url}/player_api.php?username=${creds.username}&password=${creds.password}&action=get_series"
            val response = URL(url).readText()
            val seriesList = Json.decodeFromString<List<ApiSeries>>(response)
            seriesList.map {
                Series(
                    id = it.series_id,
                    name = it.name,
                    coverUrl = it.cover,
                    backdropUrl = it.backdrop_path,
                    plot = it.plot,
                    rating = it.rating?.toFloatOrNull()
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}