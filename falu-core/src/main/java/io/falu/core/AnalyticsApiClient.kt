package io.falu.core

import androidx.annotation.RestrictTo
import io.falu.core.exceptions.ApiConnectionException
import io.falu.core.exceptions.ApiException
import io.falu.core.exceptions.AuthenticationException
import io.falu.core.models.AnalyticsTelemetry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.AuthenticationHeaderProvider
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsApiClient(key: String) : AbstractHttpApiClient(AnalyticsAuthProvider(key)) {

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun reportTelemetry(request: AnalyticsTelemetry): ResourceResponse<*> {
        val builder = Request.Builder()
            .addHeader(KEY_ORIGIN_HEADER, request.origin)
            .url(analyticsBaseUrl)
            .post(makeJson(request).toRequestBody(MEDIA_TYPE_JSON))

        return execute(builder, ResourceResponse::class.java)
    }

    override fun buildBackChannel(builder: OkHttpClient.Builder): OkHttpClient {
        builder
            .followRedirects(false)
            .connectTimeout(50, TimeUnit.SECONDS) // default is 50 seconds
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

        return super.buildBackChannel(builder)
    }

    companion object {
        private const val analyticsBaseUrl = "https://a.falu.io/track"
        private const val KEY_ORIGIN_HEADER = "Origin"
    }
}

internal class AnalyticsAuthProvider internal constructor(apiKey: String) : AuthenticationHeaderProvider() {
    private val key = ApiKeyValidator.get().requireValid(apiKey)

    override fun getParameter(request: Request.Builder): String {
        return key
    }
}