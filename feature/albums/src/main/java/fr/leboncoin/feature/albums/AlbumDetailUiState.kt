package fr.leboncoin.feature.albums

import fr.leboncoin.core.domain.model.Album

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data class Success(val album: Album) : AlbumDetailUiState
    data class Error(val message: String) : AlbumDetailUiState
}
