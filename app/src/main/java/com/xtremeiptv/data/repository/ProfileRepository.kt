package com.xtremeiptv.data.repository

import com.xtremeiptv.data.database.AppDatabase
import com.xtremeiptv.data.database.entity.Profile
import com.xtremeiptv.data.local.EncryptedPrefs
import com.xtremeiptv.data.network.protocol.ProtocolDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.in.Inject
import javax.in.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val database: AppDatabase,
    private val encryptedPrefs: EncryptedPrefs,
    private val protocolDetector: ProtocolDetector
) {
    private val profileDao = database.profileDao()
    
    fun getAllProfiles(): Flow<List<Profile>> = profileDao.getAllProfiles()
    
    fun getActiveProfile(): Flow<Profile?> = profileDao.getActiveProfile()
    
    suspend fun addProfile(profile: Profile): Boolean {
        return try {
            // Validate credentials before saving
            val detection = protocolDetector.detect(
                serverUrl = profile.serverUrl,
                username = profile.username,
                password = profile.password,
                macAddress = profile.macAddress
            )
            
            if (detection is ProtocolDetector.DetectionResult.Unknown) {
                return false
            }
            
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
        // Clear active flag if this was active
        val active = profileDao.getActiveProfile().first()
        if (active?.id == profile.id) {
            profileDao.clearActiveFlag()
        }
        profileDao.deleteProfile(profile)
        // Clean up profile-specific data
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
    
    suspend fun validateProfile(profile: Profile): Boolean {
        val detection = protocolDetector.detect(
            serverUrl = profile.serverUrl,
            username = profile.username,
            password = profile.password,
            macAddress = profile.macAddress
        )
        return detection !is ProtocolDetector.DetectionResult.Unknown
    }
    
    fun getProtocolType(profile: Profile): String {
        return when {
            !profile.username.isNullOrEmpty() && !profile.password.isNullOrEmpty() && profile.macAddress.isNullOrEmpty() -> "xtream"
            !profile.username.isNullOrEmpty() && !profile.password.isNullOrEmpty() && !profile.macAddress.isNullOrEmpty() -> "stalker"
            !profile.macAddress.isNullOrEmpty() && profile.username.isNullOrEmpty() -> "mac"
            else -> "m3u"
        }
    }
}
