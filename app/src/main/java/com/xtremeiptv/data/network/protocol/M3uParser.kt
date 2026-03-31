package com.xtremeiptv.data.network.protocol

import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Episode
import com.xtremeiptv.data.network.model.Season
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uParser @Inject constructor() {
    
    data class ParseResult(
        val channels: List<Channel>,
        val vodMovies: List<VodItem>,
        val vodSeries: List<Series>
    )
    
    suspend fun parse(content: String): ParseResult = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        val movies = mutableListOf<VodItem>()
        val seriesMap = mutableMapOf<String, MutableList<Episode>>()
        
        val lines = content.lines()
        var i = 0
        
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val infoLine = line
                val urlLine = if (i + 1 < lines.size) lines[i + 1].trim() else ""
                
                val name = extractName(infoLine)
                val groupTitle = extractGroupTitle(infoLine)
                val logo = extractLogo(infoLine)
                
                val isMovie = groupTitle?.contains("MOVIE", ignoreCase = true) == true ||
                              groupTitle?.contains("PELICULA", ignoreCase = true) == true
                val isSeries = groupTitle?.contains("SERIES", ignoreCase = true) == true ||
                               groupTitle?.contains("SHOW", ignoreCase = true) == true
                
                when {
                    isSeries -> {
                        val seriesTitle = extractSeriesTitle(infoLine) ?: name
                        val episodeNum = extractEpisodeNumber(infoLine)
                        val seasonNum = extractSeasonNumber(infoLine)
                        
                        val episode = Episode(
                            id = UUID.randomUUID().toString(),
                            title = name,
                            streamUrl = urlLine,
                            episodeNumber = episodeNum,
                            seasonNumber = seasonNum,
                            plot = null,
                            duration = null,
                            thumbnailUrl = logo
                        )
                        seriesMap.getOrPut(seriesTitle) { mutableListOf() }.add(episode)
                    }
                    isMovie -> {
                        movies.add(
                            VodItem(
                                id = UUID.randomUUID().toString(),
                                title = name,
                                streamUrl = urlLine,
                                posterUrl = logo,
                                plot = null,
                                duration = null
                            )
                        )
                    }
                    else -> {
                        channels.add(
                            Channel(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                streamUrl = urlLine,
                                logoUrl = logo,
                                groupTitle = groupTitle
                            )
                        )
                    }
                }
                i += 2
            } else {
                i++
            }
        }
        
        val series = seriesMap.map { (title, episodes) ->
            val seasons = episodes.groupBy { it.seasonNumber }.map { (seasonNum, seasonEpisodes) ->
                Season(
                    seasonNumber = seasonNum,
                    episodes = seasonEpisodes.sortedBy { it.episodeNumber }
                )
            }.sortedBy { it.seasonNumber }
            
            Series(
                id = UUID.randomUUID().toString(),
                name = title,
                seasons = seasons
            )
        }
        
        ParseResult(channels, movies, series)
    }
    
    private fun extractName(line: String): String {
        val regex = """,([^,]+)$""".toRegex()
        val raw = regex.find(line)?.groupValues?.get(1) ?: "Unknown"
        return try {
            URLDecoder.decode(raw, "UTF-8").trim()
        } catch (e: Exception) {
            raw.trim()
        }
    }
    
    private fun extractGroupTitle(line: String): String? {
        val regex = """group-title="([^"]+)"""".toRegex()
        val raw = regex.find(line)?.groupValues?.get(1)
        return raw?.let {
            try {
                URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
    }
    
    private fun extractLogo(line: String): String? {
        val regex = """tvg-logo="([^"]+)"""".toRegex()
        return regex.find(line)?.groupValues?.get(1)
    }
    
    private fun extractSeasonNumber(line: String): Int {
        val regex = """season="?(\d+)"?""".toRegex()
        return regex.find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }
    
    private fun extractEpisodeNumber(line: String): Int {
        val regex = """episode="?(\d+)"?""".toRegex()
        return regex.find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }
    
    private fun extractSeriesTitle(line: String): String? {
        val regex = """series-title="([^"]+)"""".toRegex()
        val raw = regex.find(line)?.groupValues?.get(1)
        return raw?.let {
            try {
                URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
    }
}
