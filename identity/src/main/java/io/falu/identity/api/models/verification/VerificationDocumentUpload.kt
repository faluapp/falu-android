package io.falu.identity.api.models.verification

import android.os.Parcelable
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationDocumentUpload(
    var type: IdentityDocumentType,
    var front: VerificationDocumentSide,
    var back: VerificationDocumentSide? = null
) : Parcelable

@Parcelize
internal data class VerificationDocumentSide(
    var method: UploadMethod,
    var score: Int? = null,
    var file: String
) : Parcelable