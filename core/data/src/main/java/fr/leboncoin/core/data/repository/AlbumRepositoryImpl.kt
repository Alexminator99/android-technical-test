package fr.leboncoin.core.data.repository

import fr.leboncoin.core.data.mapper.toDomain
import fr.leboncoin.core.data.mapper.toEntity
import fr.leboncoin.core.database.dao.AlbumDao
import fr.leboncoin.core.domain.repository.AlbumRepository
import fr.leboncoin.core.domain.util.DataError
import fr.leboncoin.core.domain.util.EmptyResult
import fr.leboncoin.core.domain.util.Result
import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.core.network.api.AlbumApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val api: AlbumApiService,
    private val dao: AlbumDao,
) : AlbumRepository {

    override fun getAlbums(): Flow<List<Album>> =
        dao.getAlbums().map { entities -> entities.map { it.toDomain() } }

    override fun getAlbumById(albumId: Int): Flow<Album> =
        dao.getAlbumById(albumId).map { entity ->
            entity?.toDomain() ?: throw NoSuchElementException("Album $albumId not found")
        }

    override suspend fun refreshAlbums(): EmptyResult<DataError> {
        return try {
            val dtos = api.getAlbums()
            dao.upsertFromNetworkBatch(dtos.map { it.toEntity() })
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(DataError.Network.SERVER_ERROR)
        } catch (e: SerializationException) {
            Result.Error(DataError.Network.SERIALIZATION)
        } catch (e: SocketTimeoutException) {
            Result.Error(DataError.Network.REQUEST_TIMEOUT)
        } catch (e: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun toggleFavorite(albumId: Int): EmptyResult<DataError> {
        return try {
            dao.toggleFavorite(albumId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}
