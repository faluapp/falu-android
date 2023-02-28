package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName

internal enum class VerificationStatus {
    @SerializedName("input_required")
    INPUT_REQUIRED,

    @SerializedName("processing")
    PROCESSING,

    @SerializedName("verified")
    VERIFIED,

    @SerializedName("cancelled")
    CANCELLED
}