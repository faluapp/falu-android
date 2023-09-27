package io.falu.identity.api

import android.content.Context
import io.falu.core.ApiKeyValidator
import io.falu.core.ApiVersion
import io.falu.core.ApiVersionInterceptor
import io.falu.core.AppDetailsInterceptor
import io.falu.core.exceptions.ApiConnectionException
import io.falu.core.exceptions.ApiException
import io.falu.core.exceptions.AuthenticationException
import io.falu.core.models.FaluFile
import io.falu.core.utils.getMediaType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUploadRequest
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.AuthenticationHeaderProvider
import software.tingle.api.patch.JsonPatchDocument
import java.io.File
import java.util.concurrent.TimeUnit

internal class IdentityVerificationApiClient(
    private val context: Context,
    apiKey: String,
    private val enableLogging: Boolean
) : AbstractHttpApiClient(IdentityVerificationAuthProvider(apiKey)) {
    private val appDetailsInterceptor = AppDetailsInterceptor(context)
    private val apiVersionInterceptor = ApiVersionInterceptor(ApiVersion.get().code)

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun getVerification(verification: String): ResourceResponse<Verification> {
        val builder = Request.Builder()
            .url("$baseUrl/v1/identity/verifications/$verification/workflow")
            .get()

        return execute(builder, Verification::class.java)
    }

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun updateVerification(
        verification: String,
        document: JsonPatchDocument
    ): ResourceResponse<Verification> {
        val builder = Request.Builder()
            .url("$baseUrl/v1/identity/verifications/$verification/workflow")
            .patch(makeJson(document.getOperations()).toRequestBody(MEDIA_TYPE_PATH_JSON))

        return execute(builder, Verification::class.java)
    }

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun uploadIdentityDocuments(
        verification: String,
        purpose: String,
        file: File
    ): ResourceResponse<FaluFile> {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody(file.getMediaType(context)))
            .addFormDataPart("purpose", purpose)
            .addFormDataPart("owner", verification)
            .build()

        val builder = Request.Builder()
            .url("$baseUrl/v1/files")
            .post(requestBody)
        return execute(builder, FaluFile::class.java)
    }

    @Throws(
        AuthenticationException::class,
        ApiConnectionException::class,
        ApiException::class
    )
    fun submitVerificationDocuments(
        verification: String,
        request: VerificationUploadRequest
    ): ResourceResponse<Verification> {
        val builder = Request.Builder()
            .url("$baseUrl/v1/identity/verifications/$verification/workflow/submit")
            .post(makeJson(request).toRequestBody(MEDIA_TYPE_JSON))

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