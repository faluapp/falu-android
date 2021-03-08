package tingle.software.falu

internal class ApiKeyValidator {

    fun requireValid(apiKey: String?): String {
        require(!apiKey.isNullOrBlank()) {
            "Invalid Publishable Key: " +
                    "You must use a valid FALU API key to make a FALU API request. " +
                    "For more info, see https://falu.io"
        }

        require(!apiKey.startsWith("sk_")) {
            "Invalid Publishable Key: " +
                    "You are using a secret key instead of a publishable one. " +
                    "For more info, see https://falu.io"
        }

        return apiKey
    }

    internal companion object {
        private val DEFAULT = ApiKeyValidator()

        @JvmStatic
        internal fun get(): ApiKeyValidator {
            return DEFAULT
        }
    }
}