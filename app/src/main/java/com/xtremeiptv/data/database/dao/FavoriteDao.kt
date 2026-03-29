package com.xtremeiptv.data.database.dao

import androidx.room.*
import com.xtremeiptv.data.database.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE profileId = :profileId")
    fun getFavorites(profileId: String): Flow<List<Favorite>>
    
    @Query("SELECT * FROM favorites WHERE profileId = :profileId AND contentType = :type")
    fun getFavoritesByType(profileId: String, type: String): Flow<List<Favorite>>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: Favorite)
    
    @Delete
    suspend fun removeFavorite(favorite: Favorite)
    
    @Query("DELETE FROM favorites WHERE profileId = :profileId AND contentId = :contentId")
    suspend fun removeFavoriteById(profileId: String, contentId: String)
    
    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE profileId = :profileId AND contentId = :contentId")
    suspend fun isFavorite(profileId: String, contentId: String): Boolean
}
