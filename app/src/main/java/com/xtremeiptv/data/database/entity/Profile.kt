package com.xtremeiptv.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val protocolType: String, // "m3u", "xtream", "stalker", "mac"
    val serverUrl: String,
    val username: String? = null,
    val password: String? = null,
    val macAddress: String? = null,
    val isActive: Boolean = false,
    val lastUsed: Long = System.currentTimeMillis()
)
