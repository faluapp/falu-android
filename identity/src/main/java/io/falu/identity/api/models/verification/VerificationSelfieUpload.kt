package io.falu.identity.api.models.verification

import android.os.Parcelable
import io.falu.identity.api.models.CameraSettings
import io.falu.identity.api.models.UploadMethod
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationSelfieUpload(
    var type: UploadMethod,
    var file: String,
    var variance: Float,
    var camera: CameraSettings
): Parcelable