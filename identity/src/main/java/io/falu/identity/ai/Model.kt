package io.falu.identity.ai

import android.graphics.Bitmap

/**
 *
 */
internal enum class DocumentOption {
    HUDAMA_BACK,
    HUDAMA_FRONT,
    KENYA_DL_BACK,
    KENYA_DL_FRONT,
    KENYA_ID_BACK,
    KENYA_ID_FRONT,
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
internal data class DocumentDetectionOutput(
    var score: Float,
    var option: DocumentOption,
    var bitmap: Bitmap,
    var scores: MutableList<Float>
): DetectionOutput