package io.falu.identity.api.models.requirements

import com.google.gson.annotations.SerializedName

internal enum class RequirementType {
    @SerializedName("consent")
    CONSENT,

    @SerializedName("country")
    COUNTRY,

    @SerializedName(" document_type")
    DOCUMENT_TYPE,

    @SerializedName(" document_front")
    DOCUMENT_FRONT,

    @SerializedName("document_back")
    DOCUMENT_BACK,

    @SerializedName(" selfie")
    SELFIE,

    @SerializedName("video")
    VIDEO
}