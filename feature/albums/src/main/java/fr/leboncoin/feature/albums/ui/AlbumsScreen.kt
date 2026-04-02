package fr.leboncoin.feature.albums.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.core.analytics.TrackScreenViewEvent
import fr.leboncoin.core.ui.ObserveAsEvents
import fr.leboncoin.feature.albums.AlbumsAction
import fr.leboncoin.feature.albums.AlbumsEvent
import fr.leboncoin.feature.albums.AlbumsUiState
import fr.leboncoin.feature.albums.AlbumsViewModel
import fr.leboncoin.feature.albums.R

@Composable
fun AlbumsScreenRoot(
    onAlbumClick: (albumId: Int) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val retryLabel = stringResource(R.string.retry)

    TrackScreenViewEvent("Albums")

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is AlbumsEvent.ShowError -> {
                val result = snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    actionLabel = retryLabel,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onAction(AlbumsAction.OnRetry)
                }
            }
            is AlbumsEvent.NavigateToDetail -> onAlbumClick(event.albumId)
        }
    }

    AlbumsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}

@Composable
fun AlbumsScreen(
    state: AlbumsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (AlbumsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when {
            state.isLoading && state.albums.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Spinner()
                }
            }
            state.error != null && state.albums.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error.asString(),
                            style = SparkTheme.typography.body1,
                        )
                        Spacer(Modifier.height(16.dp))
                        ButtonFilled(
                            onClick = { onAction(AlbumsAction.OnRetry) },
                            text = stringResource(R.string.retry),
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = contentPadding,
                ) {
                    items(
                        items = state.albums,
                        key = { album -> album.id },
                    ) { album ->
                        AlbumItem(
                            album = album,
                            onAlbumClick = { onAction(AlbumsAction.OnAlbumClick(album)) },
                            onToggleFavorite = { onAction(AlbumsAction.OnToggleFavorite(album)) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenLoadingPreview() {
    SparkTheme {
        AlbumsScreen(
            state = AlbumsUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenContentPreview() {
    SparkTheme {
        AlbumsScreen(
            state = AlbumsUiState(albums = PreviewData.albums),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumsScreenErrorPreview() {
    SparkTheme {
        AlbumsScreen(
            state = AlbumsUiState(
                error = fr.leboncoin.core.ui.UiText.DynamicString("No internet connection"),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
