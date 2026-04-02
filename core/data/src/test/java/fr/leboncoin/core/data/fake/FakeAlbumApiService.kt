package fr.leboncoin.core.data.fake

import fr.leboncoin.core.network.api.AlbumApiService
import fr.leboncoin.core.network.model.AlbumDto
import java.io.IOException

class FakeAlbumApiService : AlbumApiService {

    var albums: List<AlbumDto> = emptyList()
    var shouldFail: Boolean = false
    var exception: Exception = IOException("Network error")

    override suspend fun getAlbums(): List<AlbumDto> {
        if (shouldFail) throw exception
        return albums
    }
}
