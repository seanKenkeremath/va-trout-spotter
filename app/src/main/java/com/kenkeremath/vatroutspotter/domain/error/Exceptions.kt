package com.kenkeremath.vatroutspotter.domain.error

import androidx.annotation.StringRes
import com.kenkeremath.vatroutspotter.R
import java.io.IOException

sealed class TroutSpotterException(
    @StringRes val messageResId: Int,
    @StringRes val bodyResId: Int
) : Exception() {
    data object GenericException : TroutSpotterException(
        messageResId = R.string.generic_error_message,
        bodyResId = R.string.generic_error_body
    ) {
        private fun readResolve(): Any = GenericException
    }

    data object NetworkException : TroutSpotterException(
        messageResId = R.string.network_error_message,
        bodyResId = R.string.network_error_body
    ) {
        private fun readResolve(): Any = NetworkException
    }
}

fun Exception.toDomainException(): TroutSpotterException {
    return when (this) {
        is TroutSpotterException -> this
        is IOException -> TroutSpotterException.NetworkException
        else -> TroutSpotterException.GenericException
    }
}

val Exception.messageResId: Int
    get() = when (this) {
        is TroutSpotterException -> messageResId
        else -> R.string.generic_error_message
    }

val Exception.bodyResId: Int
    get() = when (this) {
        is TroutSpotterException -> bodyResId
        else -> R.string.generic_error_body
    }