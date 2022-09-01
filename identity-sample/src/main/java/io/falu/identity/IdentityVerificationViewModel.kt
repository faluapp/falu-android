package io.falu.identity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import io.falu.identity.models.IdentityVerification
import io.falu.identity.models.IdentityVerificationCreationRequest
import io.falu.identity.models.IdentityVerificationOptions
import io.falu.identity.models.IdentityVerificationOptionsForDocument
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import software.tingle.api.AbstractHttpApiClient
import software.tingle.api.ResourceResponse
import software.tingle.api.authentication.EmptyAuthenticationProvider

class IdentityVerificationViewModel(application: Application) : AndroidViewModel(application) {
    private val apiClient = ApiClient()

    fun requestIdentityVerification(
        allowDrivingLicense: Boolean,
        allowPassport: Boolean,
        allowIdentityCard: Boolean,
        allowUploads: Boolean,
        requireLiveCapture: Boolean
    ) = liveData {

        val request = IdentityVerificationCreationRequest(
            options = IdentityVerificationOptions(
                allowUploads = allowUploads,
                document = IdentityVerificationOptionsForDocument(
                    live = requireLiveCapture,
                    allowed = mutableListOf<String>().also {
                        if (allowDrivingLicense) it.add(IdentityVerificationFragment.ALLOWED_TYPE_DRIVING_LICENSE)
                        if (allowPassport) it.add(IdentityVerificationFragment.ALLOWED_TYPE_PASSPORT)
                        if (allowIdentityCard) it.add(IdentityVerificationFragment.ALLOWED_TYPE_ID_CARD)
                    }
                )
            ),
            type = "document"
        )

        val response = apiClient.createIdentityVerificationSession(request)
        emit(response)
    }
}

class ApiClient : AbstractHttpApiClient(EmptyAuthenticationProvider()) {
    suspend fun createIdentityVerificationSession(request: IdentityVerificationCreationRequest): ResourceResponse<IdentityVerification> {
        val builder = Request.Builder()
            .url("https://falu-sample.herokuapp.com/identity/create-verification-session/")
            .post(makeJson(request).toRequestBody(MEDIA_TYPE_JSON))

        return executeAsync(builder, IdentityVerification::class.java)
    }
}