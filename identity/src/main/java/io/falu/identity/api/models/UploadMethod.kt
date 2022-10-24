package io.falu.identity.api.models

import com.google.gson.annotations.SerializedName

internal enum class UploadMethod {
    @SerializedName("auto")
    AUTO,

    @SerializedName("manual")
    MANUAL,

    @SerializedName("upload")
    UPLOAD
}