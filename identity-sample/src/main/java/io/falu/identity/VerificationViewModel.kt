package io.falu.identity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import io.falu.identity.models.*
import io.falu.identity.sample.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.EmptyAuthenticationProvider
import java.util.concurrent.TimeUnit

class VerificationViewModel(application: Application) : AndroidViewModel(application) {
    private val apiClient = ApiClient()

    fun requestIdentityVerification(
        allowDrivingLicense: Boolean,
        allowPassport: Boolean,
        allowIdentityCard: Boolean,
        allowUploads: Boolean,
        allowDocumentSelfie: Boolean
    ) = liveData {

        val request = IdentityVerificationCreationRequest(
            options = IdentityVerificationOptions(
                allowUploads = allowUploads,
                document = generateDocumentOptions(
                    allowDrivingLicense,
                    allowPassport,
                    allowIdentityCard
                ),
                selfie = if (allowDocumentSelfie) IdentityVerificationOptionsForSelfie() else null,
            ),
            type = getVerificationType(allowDocumentSelfie)
        )

        val response = apiClient.createIdentityVerification(request)
        emit(response)
    }

    private fun generateDocumentOptions(
        allowDrivingLicense: Boolean,
        allowPassport: Boolean,
        allowIdentityCard: Boolean,
    ): IdentityVerificationOptionsForDocument {
        return IdentityVerificationOptionsForDocument(
            allowed = mutableListOf<String>().also {
                if (allowDrivingLicense) it.add(ALLOWED_TYPE_DRIVING_LICENSE)
                if (allowPassport) it.add(ALLOWED_TYPE_PASSPORT)
                if (allowIdentityCard) it.add(ALLOWED_TYPE_ID_CARD)
            }
        )
    }

    private fun getVerificationType(requireMatchingSelfie: Boolean): String {
        return if (requireMatchingSelfie) {
            VERIFICATION_TYPE_DOCUMENT_AND_SELFIE
        } else {
            VERIFICATION_TYPE_DOCUMENT
        }
    }

    companion object {
        const val ALLOWED_TYPE_DRIVING_LICENSE = "driving_license"
        const val ALLOWED_TYPE_PASSPORT = "passport"
        const val ALLOWED_TYPE_ID_CARD = "id_card"
        const val VERIFICATION_TYPE_DOCUMENT = "document"
        const val VERIFICATION_TYPE_DOCUMENT_AND_SELFIE = "document_and_selfie"
    }
}

class ApiClient : AbstractHttpApiClient(EmptyAuthenticationProvider()) {
    suspend fun createIdentityVerification(request: IdentityVerificationCreationRequest): ResourceResponse<IdentityVerification> {
        val builder = Request.Builder()
            .url("https://falu-samples-python.azurewebsites.net/api/IdentityVerification")
            .post(makeJson(request).toRequestBody(MEDIA_TYPE_JSON))

        return executeAsync(builder, IdentityVerification::class.java)
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
}