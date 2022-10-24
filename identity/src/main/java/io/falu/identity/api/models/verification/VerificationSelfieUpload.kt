package io.falu.identity.api.models.verification

import io.falu.identity.api.models.CameraSettings
import io.falu.identity.api.models.UploadMethod

internal data class VerificationSelfieUpload(
    var type: UploadMethod,
    var file: String,
    var variance: Float,
    var camera: CameraSettings
)