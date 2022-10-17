package io.falu.identity.api.models.verification

import io.falu.identity.api.models.CameraSettings
import io.falu.identity.api.models.UploadType

internal data class VerificationSelfieUpload(
    var type: UploadType,
    var file: String,
    var variance: Float,
    var camera: CameraSettings
)