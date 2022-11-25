package io.falu.identity.ai

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 */
internal enum class DocumentOption {
    KENYA_DL_BACK,
    KENYA_DL_FRONT,
    KENYA_ID_BACK,
    KENYA_ID_FRONT,
    KENYA_PASSPORT,
    INVALID;
}

/**
 *
 */
internal data class BoundingBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

/**
 *
 */
internal interface DetectionOutput

/***/
@Parcelize
internal data class DocumentDetectionOutput(
    var score: Float,
    var option: DocumentOption,
    var bitmap: Bitmap,
    var scores: MutableList<Float>
): DetectionOutput, Parcelable