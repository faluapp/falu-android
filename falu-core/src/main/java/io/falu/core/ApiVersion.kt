package io.falu.core

import androidx.annotation.RestrictTo

/**
 * A class that represents a FALU API version.
 *
 * See [https://falu.io)
 * for documentation on API versioning.
 *
 * See [https://falu.io) for latest
 * API changes.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ApiVersion internal constructor(internal val version: String) {

    val code: String
        get() = version

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    companion object {
        private const val API_VERSION_CODE: String = "2022-09-01"

        private val INSTANCE = ApiVersion(API_VERSION_CODE)

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @JvmSynthetic
        fun get(): ApiVersion {
            return INSTANCE
        }
    }
}