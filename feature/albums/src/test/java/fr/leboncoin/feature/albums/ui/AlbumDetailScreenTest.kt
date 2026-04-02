package fr.leboncoin.feature.albums.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.feature.albums.AlbumDetailAction
import fr.leboncoin.feature.albums.AlbumDetailUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlbumDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val album = Album(
        id = 1,
        albumId = 2,
        title = "Detail Album",
        url = "https://example.com/600.jpg",
        thumbnailUrl = "https://example.com/150.jpg",
        isFavorite = false,
    )

    @Test
    fun `when success, displays album title`() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    state = AlbumDetailUiState.Success(album),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Detail Album").assertIsDisplayed()
        composeTestRule.onNodeWithText("Album #2").assertIsDisplayed()
    }

    @Test
    fun `when error, displays error message`() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    state = AlbumDetailUiState.Error("Something went wrong"),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }

    @Test
    fun `when back clicked, OnBackClick action is triggered`() {
        var capturedAction: AlbumDetailAction? = null
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    state = AlbumDetailUiState.Success(album),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = { capturedAction = it },
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(capturedAction is AlbumDetailAction.OnBackClick)
    }

    @Test
    fun `when favorite clicked, OnToggleFavorite action is triggered`() {
        var capturedAction: AlbumDetailAction? = null
        composeTestRule.setContent {
            SparkTheme {
                AlbumDetailScreen(
                    state = AlbumDetailUiState.Success(album),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = { capturedAction = it },
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()
        assertTrue(capturedAction is AlbumDetailAction.OnToggleFavorite)
    }
}
