package io.falu.android

/**
 * A class that represents a FALU API version.
 *
 * See [https://falu.io)
 * for documentation on API versioning.
 *
 * See [https://falu.io) for latest
 * API changes.
 */
internal data class ApiVersion internal constructor(internal val version: String) {

    val code: String
        get() = version

    internal companion object {
        private const val API_VERSION_CODE: String = "2020-09-08"

        private val INSTANCE = ApiVersion(API_VERSION_CODE)

        @JvmSynthetic
        internal fun get(): ApiVersion {
            return INSTANCE
        }
    }
}