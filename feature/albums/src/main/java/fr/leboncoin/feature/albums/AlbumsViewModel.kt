package fr.leboncoin.feature.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.core.analytics.AnalyticsEvent
import fr.leboncoin.core.analytics.AnalyticsHelper
import fr.leboncoin.core.domain.usecase.GetAlbumsUseCase
import fr.leboncoin.core.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.core.domain.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.domain.util.Result
import fr.leboncoin.core.ui.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumsUiState())
    val state: StateFlow<AlbumsUiState> = _state.asStateFlow()

    private val _events = Channel<AlbumsEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeAlbums()
        refreshAlbums()
    }

    fun onAction(action: AlbumsAction) {
        when (action) {
            is AlbumsAction.OnAlbumClick -> {
                analyticsHelper.logAlbumSelected(action.album.id, action.album.title)
                viewModelScope.launch {
                    _events.send(AlbumsEvent.NavigateToDetail(action.album.id))
                }
            }
            is AlbumsAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    when (val result = toggleFavoriteUseCase(action.album.id)) {
                        is Result.Success -> {
                            analyticsHelper.logAlbumFavoriteToggled(
                                action.album.id,
                                !action.album.isFavorite,
                            )
                        }
                        is Result.Error -> {
                            _events.send(AlbumsEvent.ShowError(result.error.toUiText()))
                        }
                    }
                }
            }
            AlbumsAction.OnRefresh -> refreshAlbums()
            AlbumsAction.OnRetry -> refreshAlbums()
        }
    }

    private fun observeAlbums() {
        viewModelScope.launch {
            getAlbumsUseCase().collect { albums ->
                _state.update { it.copy(albums = albums, isLoading = false) }
            }
        }
    }

    private fun refreshAlbums() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = refreshAlbumsUseCase()) {
                is Result.Success -> {
                    analyticsHelper.logEvent(AnalyticsEvent("albums_refreshed"))
                }
                is Result.Error -> {
                    val errorText = result.error.toUiText()
                    if (_state.value.albums.isEmpty()) {
                        _state.update { it.copy(isLoading = false, error = errorText) }
                    } else {
                        _state.update { it.copy(isLoading = false) }
                        _events.send(AlbumsEvent.ShowError(errorText))
                    }
                }
            }
        }
    }
}
