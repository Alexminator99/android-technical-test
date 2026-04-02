package fr.leboncoin.feature.albums

import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.core.ui.UiText

data class AlbumsUiState(
    val isLoading: Boolean = false,
    val albums: List<Album> = emptyList(),
    val error: UiText? = null,
)
