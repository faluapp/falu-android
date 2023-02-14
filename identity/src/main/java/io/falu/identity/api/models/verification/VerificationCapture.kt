package io.falu.identity.api.models.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.falu.identity.utils.toFraction
import kotlinx.parcelize.Parcelize

/**
 *
 */
internal data class VerificationCapture(
    var timeout: Int,
    var blur: Blur?,
    var models: VerificationModel
)

/**
 *
 */
@Parcelize
internal data class VerificationModel(
    @SerializedName("document_detector")
    var document: DocumentDetector,
    @SerializedName("face_detector")
    var face: DocumentDetector?
) : Parcelable

/**
 *
 */
@Parcelize
internal data class DocumentDetector(
    var url: String,
    @SerializedName("min_score")
    var score: Int
) : Parcelable {

    val threshold: Float
        get() = score.toFraction()
}

/**
 *
 */

@Parcelize
internal data class Blur(
    @SerializedName("min_duration")
    var duration: Int,
    @SerializedName("io_u")
    var iou: Int
) : Parcelable