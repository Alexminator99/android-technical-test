package fr.leboncoin.feature.albums.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.ExperimentalSparkApi
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.card.Card
import com.adevinta.spark.components.chips.ChipTinted
import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.feature.albums.R

@OptIn(ExperimentalSparkApi::class)
@Composable
fun AlbumItem(
    album: Album,
    onAlbumClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp),
        onClick = onAlbumClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = album.title,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
            ) {
                Text(
                    text = album.title,
                    style = SparkTheme.typography.caption,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipTinted(text = stringResource(R.string.album_chip_label, album.albumId))
                    ChipTinted(text = stringResource(R.string.track_chip_label, album.id))
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = if (album.isFavorite) Icons.Filled.Favorite
                                  else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(
                        if (album.isFavorite) R.string.remove_from_favorites
                        else R.string.add_to_favorites
                    ),
                    tint = if (album.isFavorite) SparkTheme.colors.error
                           else SparkTheme.colors.onSurface,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumItemPreview() {
    SparkTheme {
        AlbumItem(
            album = PreviewData.album,
            onAlbumClick = {},
            onToggleFavorite = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumItemFavoritePreview() {
    SparkTheme {
        AlbumItem(
            album = PreviewData.album.copy(isFavorite = true),
            onAlbumClick = {},
            onToggleFavorite = {},
        )
    }
}
