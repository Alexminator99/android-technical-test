package fr.leboncoin.core.analytics

data class AnalyticsEvent(
    val type: String,
    val properties: List<Param> = emptyList(),
) {
    data class Param(val key: String, val value: String)
}
