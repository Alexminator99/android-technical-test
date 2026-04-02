package fr.leboncoin.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import fr.leboncoin.core.database.model.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY id ASC")
    fun getAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun getAlbumById(albumId: Int): Flow<AlbumEntity?>

    @Upsert
    suspend fun upsertAlbums(albums: List<AlbumEntity>)

    @Query("""
        INSERT INTO albums (id, albumId, title, url, thumbnailUrl, isFavorite)
        VALUES (:id, :albumId, :title, :url, :thumbnailUrl, 0)
        ON CONFLICT(id) DO UPDATE SET
            albumId = excluded.albumId,
            title = excluded.title,
            url = excluded.url,
            thumbnailUrl = excluded.thumbnailUrl
    """)
    suspend fun upsertFromNetwork(
        id: Int, albumId: Int, title: String, url: String, thumbnailUrl: String,
    )

    @Transaction
    suspend fun upsertFromNetworkBatch(albums: List<AlbumEntity>) {
        albums.forEach { album ->
            upsertFromNetwork(album.id, album.albumId, album.title, album.url, album.thumbnailUrl)
        }
    }

    @Query("UPDATE albums SET isFavorite = NOT isFavorite WHERE id = :albumId")
    suspend fun toggleFavorite(albumId: Int)
}
