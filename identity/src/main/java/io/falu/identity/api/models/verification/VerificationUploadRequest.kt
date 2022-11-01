package io.falu.identity.api.models.verification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationUploadRequest(
    var consent: Boolean = true,
    var country: String? = null,
    var document: VerificationDocumentUpload,
    var selfie: VerificationSelfieUpload? = null
) : Parcelable