package io.falu.identity.api.models.verification

import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadType

internal data class VerificationDocumentUpload(
    var type: IdentityDocumentType,
    var front: VerificationDocumentSide,
    var back: VerificationDocumentSide? = null
)

internal data class VerificationDocumentSide(
    var type: UploadType,
    var score: Float?,
    var file: String,
)