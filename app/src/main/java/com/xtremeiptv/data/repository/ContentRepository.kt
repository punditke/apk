package com.xtremeiptv.data.repository

import com.xtremeiptv.data.database.AppDatabase
import com.xtremeiptv.data.database.entity.Favorite
import com.xtremeiptv.data.database.entity.ResumePosition
import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import com.xtremeiptv.data.network.protocol.M3uParser
import com.xtremeiptv.data.network.protocol.MacClient
import com.xtremeiptv.data.network.protocol.StalkerClient
import com.xtremeiptv.data.network.protocol.XtreamClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val database: AppDatabase,
    private val m3uParser: M3uParser,
    private val xtreamClient: XtreamClient,
    private val stalkerClient: StalkerClient,
    private val macClient: MacClient
) {
    private val favoriteDao = database.favoriteDao()
    private val resumeDao = database.resumePositionDao()
    
    fun getFavorites(profileId: String): Flow<List<Favorite>> = favoriteDao.getFavorites(profileId)
    
    suspend fun addFavorite(profileId: String, contentId: String, contentType: String, title: String) {
        val favorite = Favorite(profileId, contentId, contentType, title)
        favoriteDao.addFavorite(favorite)
    }
    
    suspend fun removeFavorite(profileId: String, contentId: String) {
        favoriteDao.removeFavoriteById(profileId, contentId)
    }
    
    suspend fun isFavorite(profileId: String, contentId: String): Boolean {
        return favoriteDao.isFavorite(profileId, contentId)
    }
    
    fun getResumePosition(profileId: String, contentId: String): Flow<ResumePosition?> {
        return resumeDao.getResumePosition(profileId, contentId)
    }
    
    suspend fun saveResumePosition(profileId: String, contentId: String, positionMs: Long) {
        val position = ResumePosition(profileId, contentId, positionMs)
        resumeDao.saveResumePosition(position)
    }
    
    suspend fun clearResumePosition(profileId: String, contentId: String) {
        val position = resumeDao.getResumePosition(profileId, contentId).first() ?: return
        resumeDao.deleteResumePosition(position)
    }
    
    suspend fun searchChannels(channels: List<Channel>, query: String): List<Channel> {
        return channels.filter { it.name.contains(query, ignoreCase = true) }
    }
    
    suspend fun searchMovies(movies: List<VodItem>, query: String): List<VodItem> {
        return movies.filter { it.title.contains(query, ignoreCase = true) }
    }
    
    suspend fun searchSeries(series: List<Series>, query: String): List<Series> {
        return series.filter { it.name.contains(query, ignoreCase = true) }
    }
}
