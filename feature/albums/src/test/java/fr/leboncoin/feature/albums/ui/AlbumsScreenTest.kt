package fr.leboncoin.feature.albums.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.core.domain.model.Album
import fr.leboncoin.feature.albums.AlbumsAction
import fr.leboncoin.feature.albums.AlbumsUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlbumsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbums = listOf(
        Album(1, 1, "First Album", "url", "thumb", false),
        Album(2, 1, "Second Album", "url", "thumb", true),
    )

    @Test
    fun `when albums loaded, displays all album titles`() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    state = AlbumsUiState(albums = sampleAlbums),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = {},
                )
            }
        }
        composeTestRule.onNodeWithText("First Album").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Album").assertIsDisplayed()
    }

    @Test
    fun `when album clicked, OnAlbumClick action is triggered`() {
        var capturedAction: AlbumsAction? = null
        composeTestRule.setContent {
            SparkTheme {
                AlbumsScreen(
                    state = AlbumsUiState(albums = sampleAlbums),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAction = { capturedAction = it },
                )
            }
        }
        composeTestRule.onNodeWithText("First Album").performClick()
        assertTrue(capturedAction is AlbumsAction.OnAlbumClick)
    }
}
