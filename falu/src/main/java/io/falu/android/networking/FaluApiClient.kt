package io.falu.android.networking

import io.falu.android.exceptions.APIConnectionException
import io.falu.android.exceptions.APIException
import io.falu.android.exceptions.AuthenticationException
import io.falu.android.model.EvaluationRequest
import io.falu.android.model.EvaluationResponse
import io.falu.android.model.Payment
import io.falu.android.model.PaymentRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.AuthenticationHeaderProvider
import java.util.concurrent.TimeUnit

internal class FaluApiClient
internal constructor(
    publishableKey: String,
    enableLogging: Boolean
) :
    AbstractHttpApiClient(FaluAuthenticationHeaderProvider(publishableKey), enableLogging) {

    @Throws(
        AuthenticationException::class,
        APIConnectionException::class,
        APIException::class
    )
    suspend fun createEvaluation(request: EvaluationRequest): ResourceResponse<EvaluationResponse> {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "File",
                request.file.name,
                request.file.asRequestBody(MEDIA_TYPE_ALL)
            )
            .addFormDataPart("Currency", request.currency)
            .addFormDataPart("Scope", request.scope.description)
            .addFormDataPart("Provider", request.provider.desc)
            .addFormDataPart("Name", request.name)
            .addFormDataPart("Phone", request.phone ?: "")
            .addFormDataPart("Password", request.password ?: "")
            .addFormDataPart("Description", request.description ?: "")
            .build()

        val builder = Request.Builder()
            .url("$baseUrl/v1/evaluations")
            .post(requestBody)
        return executeAsync(builder, EvaluationResponse::class.java)
    }

    @Throws(
        AuthenticationException::class,
        APIConnectionException::class,
        APIException::class
    )
    suspend fun createPayment(request: PaymentRequest): ResourceResponse<Payment> {
        val builder = Request.Builder()
            .url("$baseUrl/v1/payments")
            .post(makeJson(request).toRequestBody(MEDIA_TYPE_JSON))

        return executeAsync(builder, Payment::class.java)
    }

    override fun buildBackChannel(builder: OkHttpClient.Builder): OkHttpClient {
        builder
            .followRedirects(false)
            .connectTimeout(50, TimeUnit.SECONDS) // default is 10 seconds
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        return super.buildBackChannel(builder)
    }


    companion object {
        private const val baseUrl = "https://api.falu.io"
        private val MEDIA_TYPE_ALL = "*/*".toMediaType()
    }
}

internal class FaluAuthenticationHeaderProvider internal constructor(private val publishableKey: String) :
    AuthenticationHeaderProvider() {
    override fun getParameter(request: Request.Builder): String {
        return publishableKey
    }
}