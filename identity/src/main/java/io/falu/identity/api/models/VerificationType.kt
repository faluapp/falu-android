package io.falu.identity.api.models

import com.google.gson.annotations.SerializedName

internal enum class VerificationType {
    @SerializedName("id_number")
    IDENTITY_NUMBER,

    @SerializedName("document")
    DOCUMENT,

    @SerializedName("document_and_selfie")
    DOCUMENT_AND_SELFIE,

    @SerializedName("document_and_video")
    DOCUMENT_AND_VIDEO
}