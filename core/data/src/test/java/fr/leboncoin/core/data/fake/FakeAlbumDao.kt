package fr.leboncoin.core.data.fake

import fr.leboncoin.core.database.dao.AlbumDao
import fr.leboncoin.core.database.model.AlbumEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeAlbumDao : AlbumDao {

    private val albums = MutableStateFlow<List<AlbumEntity>>(emptyList())

    override fun getAlbums(): Flow<List<AlbumEntity>> = albums

    override fun getAlbumById(albumId: Int): Flow<AlbumEntity?> =
        albums.map { list -> list.find { it.id == albumId } }

    override suspend fun upsertAlbums(albums: List<AlbumEntity>) {
        this.albums.value = albums
    }

    override suspend fun upsertFromNetwork(
        id: Int, albumId: Int, title: String, url: String, thumbnailUrl: String,
    ) {
        albums.update { list ->
            val existing = list.find { it.id == id }
            val entity = AlbumEntity(
                id = id,
                albumId = albumId,
                title = title,
                url = url,
                thumbnailUrl = thumbnailUrl,
                isFavorite = existing?.isFavorite ?: false,
            )
            if (existing != null) {
                list.map { if (it.id == id) entity else it }
            } else {
                list + entity
            }
        }
    }

    override suspend fun upsertFromNetworkBatch(albums: List<AlbumEntity>) {
        albums.forEach { album ->
            upsertFromNetwork(album.id, album.albumId, album.title, album.url, album.thumbnailUrl)
        }
    }

    override suspend fun toggleFavorite(albumId: Int) {
        albums.update { list ->
            list.map { if (it.id == albumId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }
}
