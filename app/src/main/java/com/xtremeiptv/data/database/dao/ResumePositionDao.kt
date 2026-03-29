package com.xtremeiptv.data.database.dao

import androidx.room.*
import com.xtremeiptv.data.database.entity.ResumePosition
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumePositionDao {
    @Query("SELECT * FROM resume_positions WHERE profileId = :profileId")
    fun getAllResumePositions(profileId: String): Flow<List<ResumePosition>>
    
    @Query("SELECT * FROM resume_positions WHERE profileId = :profileId AND contentId = :contentId")
    fun getResumePosition(profileId: String, contentId: String): Flow<ResumePosition?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveResumePosition(position: ResumePosition)
    
    @Delete
    suspend fun deleteResumePosition(position: ResumePosition)
    
    @Query("DELETE FROM resume_positions WHERE profileId = :profileId")
    suspend fun clearAllForProfile(profileId: String)
}
