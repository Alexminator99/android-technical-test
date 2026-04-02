package fr.leboncoin.core.analytics

fun AnalyticsHelper.logScreenView(screenName: String) {
    logEvent(
        AnalyticsEvent(
            type = "screen_view",
            properties = listOf(AnalyticsEvent.Param("screen_name", screenName)),
        )
    )
}
