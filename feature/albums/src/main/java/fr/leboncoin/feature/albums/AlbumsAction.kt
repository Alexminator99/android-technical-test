package fr.leboncoin.feature.albums

import fr.leboncoin.core.domain.model.Album

sealed interface AlbumsAction {
    data class OnAlbumClick(val album: Album) : AlbumsAction
    data class OnToggleFavorite(val album: Album) : AlbumsAction
    data object OnRefresh : AlbumsAction
    data object OnRetry : AlbumsAction
}
