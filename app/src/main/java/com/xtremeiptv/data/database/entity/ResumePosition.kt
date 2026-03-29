package com.xtremeiptv.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resume_positions", primaryKeys = ["profileId", "contentId"])
data class ResumePosition(
    val profileId: String,
    val contentId: String,
    val positionMs: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
