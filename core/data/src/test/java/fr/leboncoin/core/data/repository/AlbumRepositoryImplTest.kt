package fr.leboncoin.core.data.repository

import app.cash.turbine.test
import fr.leboncoin.core.data.fake.FakeAlbumApiService
import fr.leboncoin.core.data.fake.FakeAlbumDao
import fr.leboncoin.core.domain.util.DataError
import fr.leboncoin.core.domain.util.Result
import fr.leboncoin.core.network.model.AlbumDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class AlbumRepositoryImplTest {

    private lateinit var fakeDao: FakeAlbumDao
    private lateinit var fakeApi: FakeAlbumApiService
    private lateinit var repository: AlbumRepositoryImpl

    private val sampleDto = AlbumDto(
        id = 1,
        albumId = 1,
        title = "Test Album",
        url = "https://example.com/600.jpg",
        thumbnailUrl = "https://example.com/150.jpg",
    )

    @Before
    fun setup() {
        fakeDao = FakeAlbumDao()
        fakeApi = FakeAlbumApiService()
        repository = AlbumRepositoryImpl(fakeApi, fakeDao)
    }

    @Test
    fun `refreshAlbums persists data and getAlbums emits it`() = runTest {
        fakeApi.albums = listOf(sampleDto)

        val result = repository.refreshAlbums()
        assertTrue(result is Result.Success)

        repository.getAlbums().test {
            val albums = awaitItem()
            assertEquals(1, albums.size)
            assertEquals("Test Album", albums[0].title)
            assertEquals(1, albums[0].albumId)
            assertFalse(albums[0].isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshAlbums returns NO_INTERNET on IOException`() = runTest {
        fakeApi.shouldFail = true
        fakeApi.exception = IOException("No network")

        val result = repository.refreshAlbums()
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.NO_INTERNET, (result as Result.Error).error)
    }

    @Test
    fun `refreshAlbums returns REQUEST_TIMEOUT on SocketTimeoutException`() = runTest {
        fakeApi.shouldFail = true
        fakeApi.exception = SocketTimeoutException("Timeout")

        val result = repository.refreshAlbums()
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.REQUEST_TIMEOUT, (result as Result.Error).error)
    }

    @Test
    fun `toggleFavorite flips isFavorite and emits via getAlbums`() = runTest {
        fakeApi.albums = listOf(sampleDto)
        repository.refreshAlbums()

        repository.getAlbums().test {
            val before = awaitItem()
            assertFalse(before[0].isFavorite)

            repository.toggleFavorite(1)

            val after = awaitItem()
            assertTrue(after[0].isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAlbumById returns album when exists`() = runTest {
        fakeApi.albums = listOf(sampleDto)
        repository.refreshAlbums()

        repository.getAlbumById(1).test {
            val album = awaitItem()
            assertEquals("Test Album", album.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAlbumById throws when album does not exist`() = runTest {
        repository.getAlbumById(999).test {
            val error = awaitError()
            assertTrue(error is NoSuchElementException)
        }
    }

    @Test
    fun `refreshAlbums preserves favorites`() = runTest {
        fakeApi.albums = listOf(sampleDto)
        repository.refreshAlbums()
        repository.toggleFavorite(1)

        // Verify it's favorited
        repository.getAlbums().test {
            assertTrue(awaitItem()[0].isFavorite)
            cancelAndIgnoreRemainingEvents()
        }

        // Refresh again from network — favorites should survive
        repository.refreshAlbums()

        repository.getAlbums().test {
            val album = awaitItem()[0]
            assertTrue("Favorite should survive refresh", album.isFavorite)
            assertEquals("Test Album", album.title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
