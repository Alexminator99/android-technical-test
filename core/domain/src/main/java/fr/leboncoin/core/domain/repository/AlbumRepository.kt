package fr.leboncoin.core.domain.repository

import fr.leboncoin.core.domain.util.DataError
import fr.leboncoin.core.domain.util.EmptyResult
import fr.leboncoin.core.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAlbums(): Flow<List<Album>>
    fun getAlbumById(albumId: Int): Flow<Album>
    suspend fun refreshAlbums(): EmptyResult<DataError>
    suspend fun toggleFavorite(albumId: Int): EmptyResult<DataError>
}
