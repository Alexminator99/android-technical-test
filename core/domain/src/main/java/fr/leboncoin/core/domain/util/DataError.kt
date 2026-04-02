package fr.leboncoin.core.domain.util

sealed interface DataError : Error {
    enum class Network : DataError {
        NO_INTERNET,
        REQUEST_TIMEOUT,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
    }

    enum class Local : DataError {
        DATABASE_ERROR,
        NOT_FOUND,
        UNKNOWN,
    }
}
