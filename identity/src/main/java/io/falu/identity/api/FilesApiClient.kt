package io.falu.identity.api

import com.google.gson.Gson
import io.falu.core.ApiVersion
import io.falu.core.ApiVersionInterceptor
import io.falu.core.exceptions.ApiConnectionException
import io.falu.core.exceptions.ApiException
import io.falu.core.exceptions.AuthenticationException
import io.falu.identity.BuildConfig
import io.falu.identity.api.models.country.SupportedCountry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.EmptyAuthenticationProvider
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

internal class FilesApiClient : AbstractHttpApiClient(EmptyAuthenticationProvider()) {
    private val apiVersionInterceptor = ApiVersionInterceptor(ApiVersion.get().code)
    private val gson = Gson()

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun getSupportedCountries(): ResourceResponse<Array<SupportedCountry>> {
        val builder = Request.Builder()
            .url("$BASE_URL/v1/identity/verifications/supported_documents")
            .get()

        return execute(builder, Array<SupportedCountry>::class.java)
    }

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun downloadFile(url: String, output: File): ResourceResponse<File> {
        val builder = Request.Builder()
            .url(url)

        return downloadFile(builder, output)
    }

    override fun buildBackChannel(builder: OkHttpClient.Builder): OkHttpClient {
        builder
            .followRedirects(false)
            .addInterceptor(apiVersionInterceptor)
            .connectTimeout(50, TimeUnit.SECONDS) // default is 50 seconds
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }

        return super.buildBackChannel(builder)
    }

    private fun downloadFile(
        builder: Request.Builder,
        output: File
    ): ResourceResponse<File> {
        val request = builder.build()
        val response = backChannel.newCall(request).execute()

        var errorModel: HttpApiResponseProblem? = null

        val body = response.body
        val rc = response.code

        if (body != null) {
            when (rc) {
                200,
                201,
                204 -> {
                    body.byteStream().use { stream ->
                        FileOutputStream(output).use { stream.copyTo(it) }
                    }
                }

                400 -> errorModel =
                    gson.fromJson(body.charStream(), HttpApiResponseProblem::class.java)
            }
            body.close()
        }

        return ResourceResponse(rc, response.headers, output, errorModel)
    }

    internal companion object {
        private const val BASE_URL = "https://api.falu.io"
    }
}