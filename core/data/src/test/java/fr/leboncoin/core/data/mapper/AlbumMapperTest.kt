package fr.leboncoin.core.data.mapper

import fr.leboncoin.core.database.model.AlbumEntity
import fr.leboncoin.core.network.model.AlbumDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumMapperTest {

    @Test
    fun `AlbumDto toEntity maps all fields correctly`() {
        val dto = AlbumDto(
            id = 1,
            albumId = 2,
            title = "Test",
            url = "https://example.com/600.jpg",
            thumbnailUrl = "https://example.com/150.jpg",
        )

        val entity = dto.toEntity()

        assertEquals(1, entity.id)
        assertEquals(2, entity.albumId)
        assertEquals("Test", entity.title)
        assertEquals("https://example.com/600.jpg", entity.url)
        assertEquals("https://example.com/150.jpg", entity.thumbnailUrl)
        assertFalse(entity.isFavorite)
    }

    @Test
    fun `AlbumEntity toDomain maps all fields including isFavorite`() {
        val entity = AlbumEntity(
            id = 1,
            albumId = 2,
            title = "Test",
            url = "https://example.com/600.jpg",
            thumbnailUrl = "https://example.com/150.jpg",
            isFavorite = true,
        )

        val album = entity.toDomain()

        assertEquals(1, album.id)
        assertEquals(2, album.albumId)
        assertEquals("Test", album.title)
        assertEquals("https://example.com/600.jpg", album.url)
        assertEquals("https://example.com/150.jpg", album.thumbnailUrl)
        assertTrue(album.isFavorite)
    }
}
