package com.xtremeiptv.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xtremeiptv.data.database.entity.CachedContent
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedContentDao {
    
    @Query("SELECT * FROM cached_content WHERE profileId = :profileId")
    fun getCachedContent(profileId: String): Flow<CachedContent?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cachedContent: CachedContent)
    
    @Query("DELETE FROM cached_content WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)
    
    @Query("DELETE FROM cached_content WHERE lastUpdated < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
}
