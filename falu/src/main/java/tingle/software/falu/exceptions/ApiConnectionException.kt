package tingle.software.falu.exceptions

import java.io.IOException

class APIConnectionException(
    message: String? = null,
    cause: Throwable? = null
) : FaluException(
    cause = cause,
    message = message
) {
    internal companion object {
        @JvmSynthetic
        internal fun create(e: IOException, url: String? = null): APIConnectionException {
            val displayUrl = listOfNotNull(
                "FALU",
                "($url)".takeUnless { url.isNullOrBlank() }
            ).joinToString(" ")
            return APIConnectionException(
                "IOException during API request to $displayUrl: ${e.message}. " +
                        "Please check your internet connection and try again. " +
                        "If this problem persists, let us know at support@falu.io.",
                e
            )
        }
    }
}