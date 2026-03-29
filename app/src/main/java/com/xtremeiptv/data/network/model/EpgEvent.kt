package com.xtremeiptv.data.network.model

data class EpgEvent(
    val channelId: String,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long
)
