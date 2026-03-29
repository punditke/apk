package com.xtremeiptv.data.network.model

data class Series(
    val id: String,
    val name: String,
    val coverUrl: String? = null,
    val backdropUrl: String? = null,
    val plot: String? = null,
    val rating: Float? = null,
    val releaseDate: String? = null,
    val genres: List<String>? = null,
    val seasons: List<Season>? = null,
    val isFavorite: Boolean = false
)

data class Season(
    val seasonNumber: Int,
    val episodes: List<Episode>
)

data class Episode(
    val id: String,
    val title: String,
    val streamUrl: String,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val plot: String? = null,
    val duration: String? = null,
    val releaseDate: String? = null,
    val thumbnailUrl: String? = null
)
