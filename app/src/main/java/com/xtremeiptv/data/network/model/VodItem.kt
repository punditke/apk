package com.xtremeiptv.data.network.model

data class VodItem(
    val id: String,
    val title: String,
    val streamUrl: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val plot: String? = null,
    val duration: String? = null,
    val rating: Float? = null,
    val releaseDate: String? = null,
    val genre: String? = null,
    val isFavorite: Boolean = false
)
