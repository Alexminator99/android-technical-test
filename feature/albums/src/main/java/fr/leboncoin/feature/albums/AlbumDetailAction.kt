package fr.leboncoin.feature.albums

sealed interface AlbumDetailAction {
    data object OnBackClick : AlbumDetailAction
    data object OnToggleFavorite : AlbumDetailAction
    data object OnRetry : AlbumDetailAction
}
