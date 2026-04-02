package fr.leboncoin.core.ui

import fr.leboncoin.core.domain.util.DataError

fun DataError.toUiText(): UiText {
    val stringRes = when (this) {
        DataError.Network.NO_INTERNET -> R.string.error_no_internet
        DataError.Network.REQUEST_TIMEOUT -> R.string.error_request_timeout
        DataError.Network.SERVER_ERROR -> R.string.error_server
        DataError.Network.SERIALIZATION -> R.string.error_serialization
        DataError.Network.UNKNOWN -> R.string.error_unknown
        DataError.Local.DATABASE_ERROR -> R.string.error_database
        DataError.Local.NOT_FOUND -> R.string.error_not_found
        DataError.Local.UNKNOWN -> R.string.error_unknown
    }
    return UiText.StringResource(stringRes)
}
