package fr.leboncoin.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun TrackScreenViewEvent(
    screenName: String,
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current,
) {
    DisposableEffect(Unit) {
        analyticsHelper.logScreenView(screenName)
        onDispose {}
    }
}
