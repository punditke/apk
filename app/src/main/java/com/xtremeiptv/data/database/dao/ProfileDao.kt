package com.xtremeiptv.data.database.dao

import androidx.room.*
import com.xtremeiptv.data.database.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY lastUsed DESC")
    fun getAllProfiles(): Flow<List<Profile>>
    
    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<Profile?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)
    
    @Update
    suspend fun updateProfile(profile: Profile)
    
    @Delete
    suspend fun deleteProfile(profile: Profile)
    
    @Query("UPDATE profiles SET isActive = 0")
    suspend fun clearActiveFlag()
    
    @Query("UPDATE profiles SET isActive = 1, lastUsed = :timestamp WHERE id = :profileId")
    suspend fun setActiveProfile(profileId: String, timestamp: Long = System.currentTimeMillis())
}
