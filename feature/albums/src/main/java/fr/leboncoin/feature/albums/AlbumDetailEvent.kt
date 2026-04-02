package fr.leboncoin.feature.albums

import fr.leboncoin.core.ui.UiText

sealed interface AlbumDetailEvent {
    data object NavigateBack : AlbumDetailEvent
    data class ShowError(val message: UiText) : AlbumDetailEvent
}
