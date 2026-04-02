package fr.leboncoin.core.domain.usecase

import fr.leboncoin.core.domain.repository.AlbumRepository
import fr.leboncoin.core.domain.model.Album
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumByIdUseCase @Inject constructor(
    private val repository: AlbumRepository,
) {
    operator fun invoke(albumId: Int): Flow<Album> = repository.getAlbumById(albumId)
}
