package fr.leboncoin.core.data.mapper

import fr.leboncoin.core.database.model.AlbumEntity
import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.core.network.model.AlbumDto

fun AlbumDto.toEntity(): AlbumEntity = AlbumEntity(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
)

fun AlbumEntity.toDomain(): Album = Album(
    id = id,
    albumId = albumId,
    title = title,
    url = url,
    thumbnailUrl = thumbnailUrl,
    isFavorite = isFavorite,
)
