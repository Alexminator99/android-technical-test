package fr.leboncoin.feature.albums

import app.cash.turbine.test
import fr.leboncoin.core.analytics.AnalyticsEvent
import fr.leboncoin.core.analytics.TestAnalyticsHelper
import fr.leboncoin.core.domain.usecase.GetAlbumsUseCase
import fr.leboncoin.core.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.core.domain.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.domain.util.DataError
import fr.leboncoin.core.domain.util.Result
import fr.leboncoin.core.domain.model.Album
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    private val getAlbumsUseCase = mockk<GetAlbumsUseCase>()
    private val refreshAlbumsUseCase = mockk<RefreshAlbumsUseCase>()
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
    private val analyticsHelper = TestAnalyticsHelper()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleAlbum = Album(
        id = 1,
        albumId = 1,
        title = "Test Album",
        url = "https://example.com/600.jpg",
        thumbnailUrl = "https://example.com/150.jpg",
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        analyticsHelper.clear()
    }

    private fun createViewModel(): AlbumsViewModel {
        return AlbumsViewModel(
            getAlbumsUseCase,
            refreshAlbumsUseCase,
            toggleFavoriteUseCase,
            analyticsHelper,
        )
    }

    @Test
    fun `when initialized, albums are observed and loading completes`() = runTest {
        val albumsFlow = MutableSharedFlow<List<Album>>(replay = 1)
        every { getAlbumsUseCase() } returns albumsFlow
        coEvery { refreshAlbumsUseCase() } coAnswers {
            albumsFlow.emit(listOf(sampleAlbum))
            Result.Success(Unit)
        }

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertEquals(1, state.albums.size)
            assertEquals("Test Album", state.albums[0].title)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when refresh fails with empty list, error is shown in state`() = runTest {
        every { getAlbumsUseCase() } returns flowOf(emptyList())
        coEvery { refreshAlbumsUseCase() } returns Result.Error(DataError.Network.NO_INTERNET)

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertTrue(state.error != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when refresh fails with cached data, error event is emitted`() = runTest {
        val albumsFlow = MutableSharedFlow<List<Album>>(replay = 1)
        albumsFlow.emit(listOf(sampleAlbum))
        every { getAlbumsUseCase() } returns albumsFlow
        coEvery { refreshAlbumsUseCase() } returns Result.Error(DataError.Network.NO_INTERNET)

        val viewModel = createViewModel()

        viewModel.events.test {
            val event = awaitItem()
            assertTrue(event is AlbumsEvent.ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when album clicked, navigate event is emitted and analytics logged`() = runTest {
        every { getAlbumsUseCase() } returns flowOf(listOf(sampleAlbum))
        coEvery { refreshAlbumsUseCase() } returns Result.Success(Unit)

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AlbumsAction.OnAlbumClick(sampleAlbum))

            val event = awaitItem()
            assertTrue(event is AlbumsEvent.NavigateToDetail)
            assertEquals(1, (event as AlbumsEvent.NavigateToDetail).albumId)
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    "album_selected",
                    listOf(
                        AnalyticsEvent.Param("album_id", "1"),
                        AnalyticsEvent.Param("album_title", "Test Album"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `when favorite toggled successfully, analytics logged`() = runTest {
        every { getAlbumsUseCase() } returns flowOf(listOf(sampleAlbum))
        coEvery { refreshAlbumsUseCase() } returns Result.Success(Unit)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onAction(AlbumsAction.OnToggleFavorite(sampleAlbum))

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    "album_favorite_toggled",
                    listOf(
                        AnalyticsEvent.Param("album_id", "1"),
                        AnalyticsEvent.Param("is_favorite", "true"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `when favorite toggle fails, error event is emitted`() = runTest {
        every { getAlbumsUseCase() } returns flowOf(listOf(sampleAlbum))
        coEvery { refreshAlbumsUseCase() } returns Result.Success(Unit)
        coEvery { toggleFavoriteUseCase(1) } returns Result.Error(DataError.Local.DATABASE_ERROR)

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AlbumsAction.OnToggleFavorite(sampleAlbum))

            val event = awaitItem()
            assertTrue(event is AlbumsEvent.ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
