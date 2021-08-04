package io.falu.android.networking

import android.content.Context
import io.falu.android.ApiVersion
import io.falu.android.exceptions.APIConnectionException
import io.falu.android.exceptions.APIException
import io.falu.android.exceptions.AuthenticationException
import io.falu.android.models.evaluations.Evaluation
import io.falu.android.models.evaluations.EvaluationRequest
import io.falu.android.models.payments.Payment
import io.falu.android.models.payments.PaymentRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.AuthenticationHeaderProvider
import java.util.concurrent.TimeUnit

internal class FaluApiClient internal constructor(
    context: Context,
    publishableKey: String,
    private val enableLogging: Boolean
) : AbstractHttpApiClient(FaluAuthenticationHeaderProvider(publishableKey)) {

    private val appDetailsInterceptor = AppDetailsInterceptor(context)
    private val apiVersionInterceptor = ApiVersionInterceptor(ApiVersion.get().code)

    @Throws(
        AuthenticationException::class,
        APIConnectionException::class,
        APIException::class
    )
    suspend fun createEvaluation(request: EvaluationRequest): ResourceResponse<Evaluation> {
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
        return executeAsync(builder, Evaluation::class.java)
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
            .addInterceptor(appDetailsInterceptor)
            .addInterceptor(apiVersionInterceptor)
            .followRedirects(false)
            .connectTimeout(50, TimeUnit.SECONDS) // default is 10 seconds
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        if (enableLogging) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }

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