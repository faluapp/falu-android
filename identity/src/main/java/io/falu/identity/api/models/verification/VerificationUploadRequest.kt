package io.falu.identity.api.models.verification

internal data class VerificationUploadRequest(
    var consent: Boolean,
    var country: String,
    var document: VerificationDocumentUpload,
    var selfie: VerificationSelfieUpload? = null
)