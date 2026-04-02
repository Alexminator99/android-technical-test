package fr.leboncoin.core.analytics

class TestAnalyticsHelper : AnalyticsHelper {

    private val events = mutableListOf<AnalyticsEvent>()

    override fun logEvent(event: AnalyticsEvent) {
        events.add(event)
    }

    fun hasLogged(event: AnalyticsEvent): Boolean = event in events

    fun clear() {
        events.clear()
    }
}
