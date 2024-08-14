package io.falu.core

import androidx.annotation.RestrictTo
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Interceptor that provides retry logic based on the presence of the "X-Should-Retry" header
 * and certain HTTP status codes.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NetworkRetriesInterceptor(private val maxRetries: Int = 0) : Interceptor {

    /**
     * Intercept the request and apply retry logic based on the response.
     *
     * @param chain The request chain.
     * @return The final response after retries.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        var retryCount = 0

        while (shouldRetry(response) && retryCount < maxRetries) {
            retryCount++
            try {
                TimeUnit.MILLISECONDS.sleep(500) // Delay between retries
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Retry interrupted", e)
            }
            response = chain.proceed(request)
        }

        return response
    }

    /**
     * Determines whether a retry should be attempted based on the response.
     *
     * @param response The HTTP response.
     * @return True if the request should be retried, otherwise false.
     */
    private fun shouldRetry(response: Response): Boolean {
        val headerValue = response.header("X-Should-Retry")
        if (headerValue.toBoolean()) {
            return true
        }
        val statusCode = response.code
        return statusCode == 409 || statusCode == 408 || (statusCode in 500..599)
    }
}