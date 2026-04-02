package fr.leboncoin.core.domain.usecase

import fr.leboncoin.core.domain.repository.AlbumRepository
import fr.leboncoin.core.domain.util.DataError
import fr.leboncoin.core.domain.util.EmptyResult
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AlbumRepository,
) {
    suspend operator fun invoke(albumId: Int): EmptyResult<DataError> =
        repository.toggleFavorite(albumId)
}
