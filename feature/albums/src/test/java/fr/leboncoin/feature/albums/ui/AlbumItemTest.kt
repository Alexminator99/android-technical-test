package fr.leboncoin.feature.albums.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.adevinta.spark.SparkTheme
import fr.leboncoin.core.domain.model.Album
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlbumItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val album = Album(
        id = 1,
        albumId = 2,
        title = "Test Album Title",
        url = "https://example.com/600.jpg",
        thumbnailUrl = "https://example.com/150.jpg",
        isFavorite = false,
    )

    @Test
    fun `displays album title and chips`() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = album, onAlbumClick = {}, onToggleFavorite = {})
            }
        }
        composeTestRule.onNodeWithText("Test Album Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Album #2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Track #1").assertIsDisplayed()
    }

    @Test
    fun `when not favorite, shows add to favorites`() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(album = album, onAlbumClick = {}, onToggleFavorite = {})
            }
        }
        composeTestRule.onNodeWithContentDescription("Add to favorites").assertIsDisplayed()
    }

    @Test
    fun `when favorite, shows remove from favorites`() {
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(
                    album = album.copy(isFavorite = true),
                    onAlbumClick = {},
                    onToggleFavorite = {},
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun `clicking favorite icon triggers onToggleFavorite`() {
        var toggled = false
        composeTestRule.setContent {
            SparkTheme {
                AlbumItem(
                    album = album,
                    onAlbumClick = {},
                    onToggleFavorite = { toggled = true },
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("Add to favorites").performClick()
        assertTrue(toggled)
    }
}
