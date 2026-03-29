package com.xtremeiptv.data.network.model

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    val epgId: String? = null,
    val isFavorite: Boolean = false
)
