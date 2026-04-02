package fr.leboncoin.core.domain.model

data class Album(
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean = false,
)
