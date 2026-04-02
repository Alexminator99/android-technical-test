package fr.leboncoin.feature.albums.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.chips.ChipTinted
import com.adevinta.spark.components.progress.Spinner
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.core.analytics.TrackScreenViewEvent
import fr.leboncoin.core.ui.ObserveAsEvents
import fr.leboncoin.core.ui.UiText
import fr.leboncoin.feature.albums.AlbumDetailAction
import fr.leboncoin.feature.albums.AlbumDetailEvent
import fr.leboncoin.feature.albums.AlbumDetailUiState
import fr.leboncoin.feature.albums.AlbumDetailViewModel
import fr.leboncoin.feature.albums.R

@Composable
fun AlbumDetailScreenRoot(
    onBackClick: () -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    TrackScreenViewEvent("AlbumDetail")

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            AlbumDetailEvent.NavigateBack -> onBackClick()
            is AlbumDetailEvent.ShowError -> {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    AlbumDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalSparkApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    state: AlbumDetailUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (AlbumDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.album_details_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(AlbumDetailAction.OnBackClick) }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (state is AlbumDetailUiState.Success) {
                        IconButton(onClick = { onAction(AlbumDetailAction.OnToggleFavorite) }) {
                            Icon(
                                imageVector = if (state.album.isFavorite) Icons.Filled.Favorite
                                              else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(
                                    if (state.album.isFavorite) R.string.remove_from_favorites
                                    else R.string.add_to_favorites
                                ),
                            )
                        }
                    }
                },
            )
        },
    ) { contentPadding ->
        when (state) {
            is AlbumDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Spinner()
                }
            }
            is AlbumDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message.asString(), style = SparkTheme.typography.body1)
                        Spacer(Modifier.height(16.dp))
                        ButtonFilled(
                            onClick = { onAction(AlbumDetailAction.OnRetry) },
                            text = stringResource(R.string.retry),
                        )
                    }
                }
            }
            is AlbumDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(16.dp),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(state.album.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = state.album.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = state.album.title,
                        style = SparkTheme.typography.headline1,
                    )

                    Spacer(Modifier.height(8.dp))

                    ChipTinted(
                        text = stringResource(R.string.album_chip_label, state.album.albumId),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumDetailScreenLoadingPreview() {
    SparkTheme {
        AlbumDetailScreen(
            state = AlbumDetailUiState.Loading,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumDetailScreenSuccessPreview() {
    SparkTheme {
        AlbumDetailScreen(
            state = AlbumDetailUiState.Success(album = PreviewData.album),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumDetailScreenFavoritePreview() {
    SparkTheme {
        AlbumDetailScreen(
            state = AlbumDetailUiState.Success(
                album = PreviewData.album.copy(isFavorite = true),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumDetailScreenErrorPreview() {
    SparkTheme {
        AlbumDetailScreen(
            state = AlbumDetailUiState.Error(UiText.DynamicString("Something went wrong")),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
