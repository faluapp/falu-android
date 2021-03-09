package tingle.software.falu.networking

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.AuthenticationHeaderProvider
import tingle.software.falu.exceptions.APIConnectionException
import tingle.software.falu.exceptions.APIException
import tingle.software.falu.exceptions.AuthenticationException
import tingle.software.falu.model.EvaluationRequest
import tingle.software.falu.model.EvaluationResponse
import java.util.concurrent.TimeUnit

internal class FaluApiClient internal constructor(publishableKey: String) :
    AbstractHttpApiClient(FaluAuthenticationHeaderProvider(publishableKey)) {

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
                request.file.asRequestBody(MEDIA_TYPE_IMAGE)
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


    override fun buildBackChannel(builder: OkHttpClient.Builder): OkHttpClient {
        builder
            .followRedirects(false)
            .connectTimeout(50, TimeUnit.SECONDS) // default is 10 seconds
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)

        builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))


        return super.buildBackChannel(builder)
    }


    private companion object {
        private const val baseUrl = "https://api.falu.io"
        private val MEDIA_TYPE_IMAGE = "*/*".toMediaType()
    }
}

internal class FaluAuthenticationHeaderProvider internal constructor(private val publishableKey: String) :
    AuthenticationHeaderProvider() {
    override fun getParameter(request: Request.Builder): String {
        return publishableKey
    }
}