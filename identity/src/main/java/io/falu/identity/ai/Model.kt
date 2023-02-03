package io.falu.identity.ai

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 */
internal enum class DocumentOption {
    DL_BACK,
    DL_FRONT,
    ID_BACK,
    ID_FRONT,
    PASSPORT,
    INVALID;
}

/**
 *
 */
@Parcelize
internal data class BoundingBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) : Parcelable

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
    var box: BoundingBox,
    val rect: Rect,
    var scores: MutableList<Float>
) : DetectionOutput, Parcelable

internal data class FaceDetectionOutput(
    var score: Float
) : DetectionOutput