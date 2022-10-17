package io.falu.identity.api.models

import com.google.gson.annotations.SerializedName

internal data class CameraSettings(
    var virtual: Boolean = false,
    var lens: CameraLens,
    var brightness: Float,
    var exposure: Exposure,
)

internal data class CameraLens(
    var model: String,
    @SerializedName("focal_length")
    var focalLength: Float,
)

internal data class Exposure(
    var iso: Float,
    var duration: Float
)