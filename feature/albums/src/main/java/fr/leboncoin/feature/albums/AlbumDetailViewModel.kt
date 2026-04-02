package fr.leboncoin.feature.albums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.core.analytics.AnalyticsHelper
import fr.leboncoin.core.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.core.domain.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.domain.util.Result
import fr.leboncoin.core.ui.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlbumByIdUseCase: GetAlbumByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val albumId: Int = checkNotNull(savedStateHandle.get<Int>("albumId"))

    private val _state = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val state: StateFlow<AlbumDetailUiState> = _state.asStateFlow()

    private val _events = Channel<AlbumDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadAlbum()
    }

    fun onAction(action: AlbumDetailAction) {
        when (action) {
            AlbumDetailAction.OnBackClick -> {
                viewModelScope.launch { _events.send(AlbumDetailEvent.NavigateBack) }
            }
            AlbumDetailAction.OnRetry -> loadAlbum()
            AlbumDetailAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    when (val result = toggleFavoriteUseCase(albumId)) {
                        is Result.Success -> {
                            val currentAlbum = (_state.value as? AlbumDetailUiState.Success)?.album
                            if (currentAlbum != null) {
                                analyticsHelper.logAlbumFavoriteToggled(
                                    albumId,
                                    !currentAlbum.isFavorite,
                                )
                            }
                        }
                        is Result.Error -> {
                            _events.send(AlbumDetailEvent.ShowError(result.error.toUiText()))
                        }
                    }
                }
            }
        }
    }

    private fun loadAlbum() {
        viewModelScope.launch {
            _state.update { AlbumDetailUiState.Loading }
            getAlbumByIdUseCase(albumId)
                .catch { e ->
                    _state.update { AlbumDetailUiState.Error(e.message ?: "Unknown error") }
                }
                .collect { album ->
                    _state.update { AlbumDetailUiState.Success(album) }
                    analyticsHelper.logAlbumDetailViewed(album.id)
                }
        }
    }
}
