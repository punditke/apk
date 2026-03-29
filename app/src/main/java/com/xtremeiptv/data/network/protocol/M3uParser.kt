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
                
                val channel = parseExtinf(infoLine, urlLine)
                
                when {
                    infoLine.contains("group-title=\"MOVIE\"") || infoLine.contains("group-title=\"Movie\"") -> {
                        movies.add(
                            VodItem(
                                id = UUID.randomUUID().toString(),
                                title = channel.name,
                                streamUrl = urlLine,
                                posterUrl = extractLogo(infoLine)
                            )
                        )
                    }
                    infoLine.contains("group-title=\"SERIES\"") || infoLine.contains("group-title=\"Series\"") -> {
                        val episode = Episode(
                            id = UUID.randomUUID().toString(),
                            title = channel.name,
                            streamUrl = urlLine,
                            episodeNumber = extractEpisodeNumber(infoLine),
                            seasonNumber = extractSeasonNumber(infoLine),
                            thumbnailUrl = extractLogo(infoLine)
                        )
                        val seriesKey = extractSeriesTitle(infoLine)
                        seriesMap.getOrPut(seriesKey) { mutableListOf() }.add(episode)
                    }
                    else -> {
                        channels.add(channel)
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
    
    private fun parseExtinf(infoLine: String, url: String): Channel {
        val duration = extractDuration(infoLine)
        val name = extractName(infoLine)
        val groupTitle = extractGroupTitle(infoLine)
        val logo = extractLogo(infoLine)
        val epgId = extractEpgId(infoLine)
        
        return Channel(
            id = UUID.randomUUID().toString(),
            name = name,
            streamUrl = url,
            logoUrl = logo,
            groupTitle = groupTitle,
            epgId = epgId
        )
    }
    
    private fun extractDuration(line: String): Int {
        val regex = """#EXTINF:(-?\d+)""".toRegex()
        return regex.find(line)?.groupValues?.get(1)?.toIntOrNull() ?: -1
    }
    
    private fun extractName(line: String): String {
        val regex = """,([^,]+)$""".toRegex()
        val raw = regex.find(line)?.groupValues?.get(1) ?: "Unknown"
        return URLDecoder.decode(raw, "UTF-8").trim()
    }
    
    private fun extractGroupTitle(line: String): String? {
        val regex = """group-title="([^"]+)"""".toRegex()
        val raw = regex.find(line)?.groupValues?.get(1)
        return raw?.let { URLDecoder.decode(it, "UTF-8") }
    }
    
    private fun extractLogo(line: String): String? {
        val regex = """tvg-logo="([^"]+)"""".toRegex()
        return regex.find(line)?.groupValues?.get(1)
    }
    
    private fun extractEpgId(line: String): String? {
        val regex = """tvg-id="([^"]+)"""".toRegex()
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
    
    private fun extractSeriesTitle(line: String): String {
        val regex = """series-title="([^"]+)"""".toRegex()
        return regex.find(line)?.groupValues?.get(1)?.let { URLDecoder.decode(it, "UTF-8") } ?: "Unknown Series"
    }
}
