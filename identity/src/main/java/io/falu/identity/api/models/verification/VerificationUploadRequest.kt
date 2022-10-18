package io.falu.identity.api.models.verification

internal data class VerificationUploadRequest(
    var consent: Boolean = true,
    var country: String? = null,
    var document: VerificationDocumentUpload,
    var selfie: VerificationSelfieUpload? = null
)