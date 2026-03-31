package com.xtremeiptv.data.network.protocol

import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uLoader @Inject constructor(
    private val parser: M3uParser
) {
    
    data class M3uResult(
        val channels: List<Channel>,
        val movies: List<VodItem>,
        val series: List<Series>
    )
    
    suspend fun loadFromUrl(url: String): M3uResult = withContext(Dispatchers.IO) {
        try {
            val content = URL(url).readText()
            val result = parser.parse(content)
            M3uResult(result.channels, result.vodMovies, result.vodSeries)
        } catch (e: Exception) {
            M3uResult(emptyList(), emptyList(), emptyList())
        }
    }
    
    suspend fun loadFromFile(filePath: String): M3uResult = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(filePath)
            val content = file.readText()
            val result = parser.parse(content)
            M3uResult(result.channels, result.vodMovies, result.vodSeries)
        } catch (e: Exception) {
            M3uResult(emptyList(), emptyList(), emptyList())
        }
    }
}
