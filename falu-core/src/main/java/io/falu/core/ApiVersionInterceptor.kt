package io.falu.core

import androidx.annotation.RestrictTo
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An @[Interceptor] that adds headers for Falu's API Version to a request before sending
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ApiVersionInterceptor(private val code: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("X-Falu-Version", code)
            .build()

        return chain.proceed(request)
    }
}