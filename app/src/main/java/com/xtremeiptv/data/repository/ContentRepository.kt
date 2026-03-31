package com.xtremeiptv.data.repository

import com.xtremeiptv.data.database.AppDatabase
import com.xtremeiptv.data.database.entity.CachedContent
import com.xtremeiptv.data.database.entity.Favorite
import com.xtremeiptv.data.database.entity.ResumePosition
import com.xtremeiptv.data.network.model.Channel
import com.xtremeiptv.data.network.model.Series
import com.xtremeiptv.data.network.model.VodItem
import com.xtremeiptv.data.network.protocol.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val database: AppDatabase,
    private val m3uLoader: M3uLoader,
    private val xtreamClient: XtreamClient,
    private val stalkerClient: StalkerClient,
    private val macClient: MacClient
) {
    private val favoriteDao = database.favoriteDao()
    private val resumeDao = database.resumePositionDao()
    private val cacheDao = database.cachedContentDao()
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // ==================== CACHED CONTENT ====================
    
    suspend fun getCachedChannels(profileId: String): List<Channel> {
        val cached = cacheDao.getCachedContent(profileId).first()
        return if (cached != null && cached.channelsJson.isNotEmpty()) {
            try {
                json.decodeFromString(cached.channelsJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun getCachedMovies(profileId: String): List<VodItem> {
        val cached = cacheDao.getCachedContent(profileId).first()
        return if (cached != null && cached.moviesJson.isNotEmpty()) {
            try {
                json.decodeFromString(cached.moviesJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun getCachedSeries(profileId: String): List<Series> {
        val cached = cacheDao.getCachedContent(profileId).first()
        return if (cached != null && cached.seriesJson.isNotEmpty()) {
            try {
                json.decodeFromString(cached.seriesJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun refreshCache(profile: com.xtremeiptv.data.database.entity.Profile) {
        try {
            // Parallel fetch all three content types
            val (channels, movies, series) = coroutineScope {
                val channelsDeferred = async { loadLiveChannelsFromNetwork(profile) }
                val moviesDeferred = async { loadMoviesFromNetwork(profile) }
                val seriesDeferred = async { loadSeriesFromNetwork(profile) }
                Triple(channelsDeferred.await(), moviesDeferred.await(), seriesDeferred.await())
            }
            
            val cached = CachedContent(
                profileId = profile.id,
                protocolType = profile.protocolType,
                channelsJson = json.encodeToString(channels),
                moviesJson = json.encodeToString(movies),
                seriesJson = json.encodeToString(series),
                lastUpdated = System.currentTimeMillis()
            )
            cacheDao.insertOrUpdate(cached)
        } catch (e: Exception) {
            // Cache refresh failed, keep existing cache
        }
    }
    
    suspend fun refreshCacheFast(profile: com.xtremeiptv.data.database.entity.Profile, contentTypes: List<String> = listOf("live", "movies", "series")) {
        try {
            val channels = if ("live" in contentTypes) loadLiveChannelsFromNetwork(profile) else emptyList()
            val movies = if ("movies" in contentTypes) loadMoviesFromNetwork(profile) else emptyList()
            val series = if ("series" in contentTypes) loadSeriesFromNetwork(profile) else emptyList()
            
            val existing = cacheDao.getCachedContent(profile.id).first()
            val cached = CachedContent(
                profileId = profile.id,
                protocolType = profile.protocolType,
                channelsJson = if (channels.isNotEmpty()) json.encodeToString(channels) else existing?.channelsJson ?: "",
                moviesJson = if (movies.isNotEmpty()) json.encodeToString(movies) else existing?.moviesJson ?: "",
                seriesJson = if (series.isNotEmpty()) json.encodeToString(series) else existing?.seriesJson ?: "",
                lastUpdated = System.currentTimeMillis()
            )
            cacheDao.insertOrUpdate(cached)
        } catch (e: Exception) {
            // Cache refresh failed
        }
    }
    
    suspend fun clearCache(profileId: String) {
        cacheDao.deleteByProfileId(profileId)
    }
    
    // ==================== NETWORK LOADING ====================
    
    private suspend fun loadLiveChannelsFromNetwork(profile: com.xtremeiptv.data.database.entity.Profile): List<Channel> {
        return try {
            when (profile.protocolType) {
                "xtream" -> {
                    val creds = XtreamClient.Credentials(profile.serverUrl, profile.username ?: "", profile.password ?: "")
                    xtreamClient.getLiveChannels(creds)
                }
                "stalker" -> {
                    val creds = StalkerClient.StalkerCredentials(
                        profile.serverUrl, profile.username ?: "", profile.password ?: "", profile.macAddress ?: ""
                    )
                    stalkerClient.getLiveChannels(creds)
                }
                "mac" -> {
                    val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress ?: "")
                    macClient.getLiveChannels(creds)
                }
                "m3u" -> {
                    val m3uResult = if (profile.serverUrl.startsWith("http")) {
                        m3uLoader.loadFromUrl(profile.serverUrl)
                    } else {
                        m3uLoader.loadFromFile(profile.serverUrl)
                    }
                    m3uResult.channels
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun loadMoviesFromNetwork(profile: com.xtremeiptv.data.database.entity.Profile): List<VodItem> {
        return try {
            when (profile.protocolType) {
                "xtream" -> {
                    val creds = XtreamClient.Credentials(profile.serverUrl, profile.username ?: "", profile.password ?: "")
                    xtreamClient.getVodMovies(creds)
                }
                "stalker" -> {
                    val creds = StalkerClient.StalkerCredentials(
                        profile.serverUrl, profile.username ?: "", profile.password ?: "", profile.macAddress ?: ""
                    )
                    stalkerClient.getVodMovies(creds)
                }
                "mac" -> {
                    val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress ?: "")
                    macClient.getVodMovies(creds)
                }
                "m3u" -> {
                    val m3uResult = if (profile.serverUrl.startsWith("http")) {
                        m3uLoader.loadFromUrl(profile.serverUrl)
                    } else {
                        m3uLoader.loadFromFile(profile.serverUrl)
                    }
                    m3uResult.movies
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun loadSeriesFromNetwork(profile: com.xtremeiptv.data.database.entity.Profile): List<Series> {
        return try {
            when (profile.protocolType) {
                "xtream" -> {
                    val creds = XtreamClient.Credentials(profile.serverUrl, profile.username ?: "", profile.password ?: "")
                    xtreamClient.getSeries(creds)
                }
                "stalker" -> {
                    val creds = StalkerClient.StalkerCredentials(
                        profile.serverUrl, profile.username ?: "", profile.password ?: "", profile.macAddress ?: ""
                    )
                    stalkerClient.getSeries(creds)
                }
                "mac" -> {
                    val creds = MacClient.MacCredentials(profile.serverUrl, profile.macAddress ?: "")
                    macClient.getSeries(creds)
                }
                "m3u" -> {
                    val m3uResult = if (profile.serverUrl.startsWith("http")) {
                        m3uLoader.loadFromUrl(profile.serverUrl)
                    } else {
                        m3uLoader.loadFromFile(profile.serverUrl)
                    }
                    m3uResult.series
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ==================== PUBLIC METHODS (with cache and parallel loading) ====================
    
    suspend fun loadLiveChannels(profile: com.xtremeiptv.data.database.entity.Profile, useCache: Boolean = true): List<Channel> {
        if (useCache) {
            val cached = getCachedChannels(profile.id)
            if (cached.isNotEmpty()) {
                return cached
            }
        }
        val fresh = loadLiveChannelsFromNetwork(profile)
        if (fresh.isNotEmpty()) {
            refreshCacheFast(profile, listOf("live"))
        }
        return fresh
    }
    
    suspend fun loadMovies(profile: com.xtremeiptv.data.database.entity.Profile, useCache: Boolean = true): List<VodItem> {
        if (useCache) {
            val cached = getCachedMovies(profile.id)
            if (cached.isNotEmpty()) {
                return cached
            }
        }
        val fresh = loadMoviesFromNetwork(profile)
        if (fresh.isNotEmpty()) {
            refreshCacheFast(profile, listOf("movies"))
        }
        return fresh
    }
    
    suspend fun loadSeries(profile: com.xtremeiptv.data.database.entity.Profile, useCache: Boolean = true): List<Series> {
        if (useCache) {
            val cached = getCachedSeries(profile.id)
            if (cached.isNotEmpty()) {
                return cached
            }
        }
        val fresh = loadSeriesFromNetwork(profile)
        if (fresh.isNotEmpty()) {
            refreshCacheFast(profile, listOf("series"))
        }
        return fresh
    }
    
    // Load all content types in parallel (faster initial load)
    suspend fun loadAllContent(profile: com.xtremeiptv.data.database.entity.Profile): Triple<List<Channel>, List<VodItem>, List<Series>> {
        // Check cache first
        val cachedChannels = getCachedChannels(profile.id)
        val cachedMovies = getCachedMovies(profile.id)
        val cachedSeries = getCachedSeries(profile.id)
        
        if (cachedChannels.isNotEmpty() && cachedMovies.isNotEmpty() && cachedSeries.isNotEmpty()) {
            return Triple(cachedChannels, cachedMovies, cachedSeries)
        }
        
        // Parallel network fetch
        return coroutineScope {
            val channelsDeferred = async { loadLiveChannelsFromNetwork(profile) }
            val moviesDeferred = async { loadMoviesFromNetwork(profile) }
            val seriesDeferred = async { loadSeriesFromNetwork(profile) }
            
            val channels = channelsDeferred.await()
            val movies = moviesDeferred.await()
            val series = seriesDeferred.await()
            
            // Save to cache
            if (channels.isNotEmpty() || movies.isNotEmpty() || series.isNotEmpty()) {
                val cached = CachedContent(
                    profileId = profile.id,
                    protocolType = profile.protocolType,
                    channelsJson = json.encodeToString(channels),
                    moviesJson = json.encodeToString(movies),
                    seriesJson = json.encodeToString(series),
                    lastUpdated = System.currentTimeMillis()
                )
                cacheDao.insertOrUpdate(cached)
            }
            
            Triple(channels, movies, series)
        }
    }
    
    // ==================== FAVORITES ====================
    
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
    
    // ==================== RESUME POSITIONS ====================
    
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
}
