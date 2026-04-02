package fr.leboncoin.core.analytics

import timber.log.Timber
import javax.inject.Inject

class TimberAnalyticsHelper @Inject constructor() : AnalyticsHelper {

    override fun logEvent(event: AnalyticsEvent) {
        val params = event.properties.joinToString { "${it.key}=${it.value}" }
        Timber.d("Analytics: ${event.type} | $params")
    }
}
