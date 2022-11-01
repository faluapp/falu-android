package io.falu.identity.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CameraSettings(
    var virtual: Boolean = false,
    var lens: CameraLens,
    var brightness: Float,
    var exposure: Exposure,
) : Parcelable

@Parcelize
internal data class CameraLens(
    var model: String,
    @SerializedName("focal_length")
    var focalLength: Float,
) : Parcelable

@Parcelize
internal data class Exposure(
    var iso: Float,
    var duration: Float
) : Parcelable