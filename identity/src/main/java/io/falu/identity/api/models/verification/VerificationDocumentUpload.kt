package io.falu.identity.api.models.verification

import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod

internal data class VerificationDocumentUpload(
    var type: IdentityDocumentType,
    var front: VerificationDocumentSide,
    var back: VerificationDocumentSide? = null
)

internal data class VerificationDocumentSide(
    var method: UploadMethod,
    var score: Float? = null,
    var file: String,
)