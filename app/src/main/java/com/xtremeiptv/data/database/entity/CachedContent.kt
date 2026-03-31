package com.xtremeiptv.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "cached_content")
data class CachedContent(
    @PrimaryKey
    val profileId: String,
    val protocolType: String,
    val channelsJson: String = "",
    val moviesJson: String = "",
    val seriesJson: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
