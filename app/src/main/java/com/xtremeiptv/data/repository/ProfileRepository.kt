package com.xtremeiptv.data.repository

import com.xtremeiptv.data.database.AppDatabase
import com.xtremeiptv.data.database.entity.Profile
import com.xtremeiptv.data.local.EncryptedPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val database: AppDatabase,
    private val encryptedPrefs: EncryptedPrefs
) {
    private val profileDao = database.profileDao()
    
    fun getAllProfiles(): Flow<List<Profile>> = profileDao.getAllProfiles()
    
    fun getActiveProfile(): Flow<Profile?> = profileDao.getActiveProfile()
    
    suspend fun addProfile(profile: Profile): Boolean {
        return try {
            profileDao.insertProfile(profile)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile)
    }
    
    suspend fun deleteProfile(profile: Profile) {
        val active = profileDao.getActiveProfile().first()
        if (active?.id == profile.id) {
            profileDao.clearActiveFlag()
        }
        profileDao.deleteProfile(profile)
        encryptedPrefs.remove("profile_${profile.id}_last_content")
    }
    
    suspend fun setActiveProfile(profileId: String) {
        profileDao.clearActiveFlag()
        profileDao.setActiveProfile(profileId)
        encryptedPrefs.putString("last_active_profile", profileId)
    }
    
    suspend fun getLastActiveProfile(): String? {
        return encryptedPrefs.getString("last_active_profile")
    }
}