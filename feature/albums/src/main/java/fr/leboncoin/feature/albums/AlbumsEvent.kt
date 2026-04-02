package fr.leboncoin.feature.albums

import fr.leboncoin.core.ui.UiText

sealed interface AlbumsEvent {
    data class ShowError(val message: UiText) : AlbumsEvent
    data class NavigateToDetail(val albumId: Int) : AlbumsEvent
}
