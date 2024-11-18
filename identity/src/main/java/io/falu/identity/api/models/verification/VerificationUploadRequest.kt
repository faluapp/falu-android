package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName

internal data class VerificationUploadRequest(
    var consent: Boolean = true,
    var country: String? = null,
    var document: VerificationDocumentUpload? = null,
    var selfie: VerificationSelfieUpload? = null,
    @SerializedName("id_number")
    var idNumber: VerificationIdNumberUpload? = null,
    @SerializedName("tax_id")
    var taxPin: VerificationTaxPinUpload? = null
)