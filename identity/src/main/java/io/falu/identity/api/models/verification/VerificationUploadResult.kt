package io.falu.identity.api.models.verification

import android.os.Parcelable
import io.falu.core.models.FaluFile
import io.falu.identity.api.models.UploadMethod
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class VerificationUploadResult(
    var file: FaluFile,
    var score: Int? = null,
    var method: UploadMethod? = null
) : Parcelable
