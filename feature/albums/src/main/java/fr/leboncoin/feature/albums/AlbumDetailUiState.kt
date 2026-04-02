package fr.leboncoin.feature.albums

import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.core.ui.UiText

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data class Success(val album: Album) : AlbumDetailUiState
    data class Error(val message: UiText) : AlbumDetailUiState
}
