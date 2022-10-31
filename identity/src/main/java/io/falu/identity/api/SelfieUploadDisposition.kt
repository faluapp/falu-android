package io.falu.identity.api

import io.falu.identity.api.models.verification.VerificationUploadResult

internal data class SelfieUploadDisposition(
    var result: VerificationUploadResult? = null
) {

    fun update(result: VerificationUploadResult) =
        this.copy(result = result)

    val isUploaded: Boolean
        get() = result != null
}