package io.falu.identity.ai

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.max
import kotlin.math.min

/**
 *
 */
internal enum class DocumentOption {
    DL_BACK,
    DL_FRONT,
    ID_BACK,
    ID_FRONT,
    PASSPORT,
    INVALID
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
    var score: Float,
    var bitmap: Bitmap,
    var box: BoundingBox,
    val rect: Rect
) : DetectionOutput

/**
 * Measure the accuracy of detection
 */
internal fun calculateIOU(currentBox: BoundingBox, previousBox: BoundingBox): Float {
    val currentLeft = currentBox.left
    val currentRight = currentBox.left + currentBox.width
    val currentTop = currentBox.top
    val currentBottom = currentBox.top + currentBox.height

    val previousLeft = previousBox.left
    val previousRight = previousBox.left + previousBox.width
    val previousTop = previousBox.top
    val previousBottom = previousBox.top + previousBox.height

    // determine the (x, y)-coordinates of the intersection rectangle
    val xA = max(currentLeft, previousLeft)
    val yA = max(currentTop, previousTop)
    val xB = min(currentRight, previousRight)
    val yB = min(currentBottom, previousBottom)

    // compute the area of intersection rectangle

    // compute the area of intersection rectangle
    val intersectionArea = (xB - xA) * (yB - yA)

    // compute the area of both the prediction and ground-truth
    // rectangles
    val currentBoxArea = (currentRight - currentLeft) * (currentBottom - currentTop)
    val previousBoxArea = (previousRight - previousLeft) * (previousBottom - previousTop)

    // compute the intersection over union by taking the intersection
    // area and dividing it by the sum of prediction + ground-truth
    // areas - the intersection area

    return intersectionArea / (currentBoxArea + previousBoxArea - intersectionArea)
}