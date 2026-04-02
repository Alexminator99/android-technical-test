package fr.leboncoin.feature.albums

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import fr.leboncoin.core.analytics.AnalyticsEvent
import fr.leboncoin.core.analytics.TestAnalyticsHelper
import fr.leboncoin.core.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.core.domain.usecase.ToggleFavoriteUseCase
import fr.leboncoin.core.domain.util.DataError
import fr.leboncoin.core.domain.util.Result
import fr.leboncoin.core.domain.model.Album
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    private val getAlbumByIdUseCase = mockk<GetAlbumByIdUseCase>()
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
    private val analyticsHelper = TestAnalyticsHelper()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleAlbum = Album(
        id = 42,
        albumId = 5,
        title = "Detail Album",
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

    private fun createSavedStateHandle(): SavedStateHandle {
        return SavedStateHandle(mapOf("albumId" to 42))
    }

    private fun createViewModel(): AlbumDetailViewModel {
        return AlbumDetailViewModel(
            createSavedStateHandle(),
            getAlbumByIdUseCase,
            toggleFavoriteUseCase,
            analyticsHelper,
        )
    }

    @Test
    fun `when loaded, state transitions to Success and analytics logged`() = runTest {
        every { getAlbumByIdUseCase(42) } returns flowOf(sampleAlbum)

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertTrue(state is AlbumDetailUiState.Success)
            assertEquals("Detail Album", (state as AlbumDetailUiState.Success).album.title)
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    "album_detail_viewed",
                    listOf(
                        AnalyticsEvent.Param("album_id", "42"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `when album not found, state shows Error`() = runTest {
        every { getAlbumByIdUseCase(42) } returns flow {
            throw NoSuchElementException("Album 42 not found")
        }

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = expectMostRecentItem()
            assertTrue(state is AlbumDetailUiState.Error)
            assertEquals("Album 42 not found", (state as AlbumDetailUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when back clicked, NavigateBack event is emitted`() = runTest {
        every { getAlbumByIdUseCase(42) } returns flowOf(sampleAlbum)

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AlbumDetailAction.OnBackClick)

            val event = awaitItem()
            assertTrue(event is AlbumDetailEvent.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when favorite toggled, analytics logged`() = runTest {
        every { getAlbumByIdUseCase(42) } returns flowOf(sampleAlbum)
        coEvery { toggleFavoriteUseCase(42) } returns Result.Success(Unit)

        val viewModel = createViewModel()

        viewModel.onAction(AlbumDetailAction.OnToggleFavorite)

        assertTrue(
            analyticsHelper.hasLogged(
                AnalyticsEvent(
                    "album_favorite_toggled",
                    listOf(
                        AnalyticsEvent.Param("album_id", "42"),
                        AnalyticsEvent.Param("is_favorite", "true"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `when favorite toggle fails, ShowError event is emitted`() = runTest {
        every { getAlbumByIdUseCase(42) } returns flowOf(sampleAlbum)
        coEvery { toggleFavoriteUseCase(42) } returns Result.Error(DataError.Local.DATABASE_ERROR)

        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(AlbumDetailAction.OnToggleFavorite)

            val event = awaitItem()
            assertTrue(event is AlbumDetailEvent.ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
