package io.falu.core.exceptions

import androidx.annotation.RestrictTo
import java.io.IOException

class ApiConnectionException(
    message: String? = null,
    cause: Throwable? = null
) : FaluException(
    cause = cause,
    message = message
) {
    companion object {
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @JvmSynthetic
        fun create(e: IOException, url: String? = null): ApiConnectionException {
            val displayUrl = listOfNotNull(
                "FALU",
                "($url)".takeUnless { url.isNullOrBlank() }
            ).joinToString(" ")
            return ApiConnectionException(
                "IOException during API request to $displayUrl: ${e.message}. " +
                        "Please check your internet connection and try again. " +
                        "If this problem persists, let us know at support@falu.io.",
                e
            )
        }
    }
}