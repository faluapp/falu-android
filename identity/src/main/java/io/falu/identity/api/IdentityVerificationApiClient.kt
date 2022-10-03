package io.falu.identity.api

import android.content.Context
import io.falu.core.ApiKeyValidator
import io.falu.core.ApiVersion
import io.falu.core.ApiVersionInterceptor
import io.falu.core.exceptions.APIConnectionException
import io.falu.core.exceptions.APIException
import io.falu.core.exceptions.AuthenticationException
import io.falu.identity.api.models.Verification
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.AuthenticationHeaderProvider
import java.util.concurrent.TimeUnit

internal class IdentityVerificationApiClient(
    context: Context,
    apiKey: String,
    private val enableLogging: Boolean
) : AbstractHttpApiClient(IdentityVerificationAuthProvider(apiKey)) {
    private val appDetailsInterceptor = AppDetailsInterceptor(context)
    private val apiVersionInterceptor = ApiVersionInterceptor(ApiVersion.get().code)

    @Throws(
        AuthenticationException::class,
        APIConnectionException::class,
        APIException::class
    )
    fun getVerification(): ResourceResponse<Verification> {
        val builder = Request.Builder()
            .url("$baseUrl/v1/verification")
            .get()

        return execute(builder, Verification::class.java)
    }

    override fun buildBackChannel(builder: OkHttpClient.Builder): OkHttpClient {
        builder
            .addInterceptor(appDetailsInterceptor)
            .addInterceptor(apiVersionInterceptor)
            .followRedirects(false)
            .connectTimeout(50, TimeUnit.SECONDS) // default is 50 seconds
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        if (enableLogging) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }

        return super.buildBackChannel(builder)
    }

    internal companion object {
        private const val baseUrl = "https://api.falu.io"
    }
}

internal class IdentityVerificationAuthProvider internal constructor(apiKey: String) :
    AuthenticationHeaderProvider() {
    private val temporaryKey = ApiKeyValidator.get().requireValid(apiKey)

    override fun getParameter(request: Request.Builder): String {
        return temporaryKey
    }
}