package com.xtremeiptv.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "favorites", primaryKeys = ["profileId", "contentId", "contentType"])
data class Favorite(
    val profileId: String,
    val contentId: String,
    val contentType: String, // "live", "movie", "series"
    val title: String,
    val addedAt: Long = System.currentTimeMillis()
)
