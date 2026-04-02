package fr.leboncoin.feature.albums

import fr.leboncoin.core.analytics.AnalyticsEvent
import fr.leboncoin.core.analytics.AnalyticsHelper

fun AnalyticsHelper.logAlbumSelected(albumId: Int, title: String) {
    logEvent(
        AnalyticsEvent(
            type = "album_selected",
            properties = listOf(
                AnalyticsEvent.Param("album_id", albumId.toString()),
                AnalyticsEvent.Param("album_title", title),
            ),
        )
    )
}

fun AnalyticsHelper.logAlbumDetailViewed(albumId: Int) {
    logEvent(
        AnalyticsEvent(
            type = "album_detail_viewed",
            properties = listOf(
                AnalyticsEvent.Param("album_id", albumId.toString()),
            ),
        )
    )
}

fun AnalyticsHelper.logAlbumFavoriteToggled(albumId: Int, isFavorite: Boolean) {
    logEvent(
        AnalyticsEvent(
            type = "album_favorite_toggled",
            properties = listOf(
                AnalyticsEvent.Param("album_id", albumId.toString()),
                AnalyticsEvent.Param("is_favorite", isFavorite.toString()),
            ),
        )
    )
}
