package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName

internal data class VerificationUpdateOptions(
    val consent: Boolean? = null,
    val document: VerificationDocumentUpload? = null,
    val country: String? = null,
    val selfie: VerificationSelfieUpload? = null,
    @SerializedName("id_number")
    val idNumber: VerificationIdNumberUpload? = null
)