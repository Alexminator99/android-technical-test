package fr.leboncoin.feature.albums.ui

import fr.leboncoin.core.domain.model.Album

internal object PreviewData {
    val album = Album(
        id = 1,
        albumId = 1,
        title = "accusamus beatae ad facilis cum similique qui sunt",
        url = "https://via.placeholder.com/600/92c952",
        thumbnailUrl = "https://via.placeholder.com/150/92c952",
        isFavorite = false,
    )

    val albums = (1..5).map { i ->
        album.copy(
            id = i,
            albumId = (i / 3) + 1,
            title = "Album track title #$i",
            isFavorite = i % 2 == 0,
        )
    }
}
