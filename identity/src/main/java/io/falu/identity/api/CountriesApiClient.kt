package io.falu.identity.api

import io.falu.core.exceptions.APIConnectionException
import io.falu.core.exceptions.APIException
import io.falu.core.exceptions.AuthenticationException
import io.falu.identity.BuildConfig
import io.falu.identity.api.models.country.SupportedCountry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.EmptyAuthenticationProvider
import java.util.concurrent.TimeUnit

internal class CountriesApiClient : AbstractHttpApiClient(EmptyAuthenticationProvider()) {

    @Throws(
        AuthenticationException::class,
        APIConnectionException::class,
        APIException::class
    )
    fun getSupportedCountries(): ResourceResponse<Array<SupportedCountry>> {
        val builder = Request.Builder()
            .url("${baseUrl}/identity/supported-countries.json")
            .get()

        return execute(builder, Array<SupportedCountry>::class.java)
    }

    override fun buildBackChannel(builder: OkHttpClient.Builder): OkHttpClient {
        builder
            .followRedirects(false)
            .connectTimeout(30, TimeUnit.SECONDS) // default is 30 seconds
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }

        return super.buildBackChannel(builder)
    }

    internal companion object {
        private const val baseUrl = "https://cdn.falu.io"
    }
}