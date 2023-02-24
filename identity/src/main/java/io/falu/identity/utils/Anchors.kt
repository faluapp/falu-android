package io.falu.identity.utils

import android.util.Size

private const val numberOfLayers = 4
private const val anchorOffsetX = 0.5f
private const val anchorOffsetY = 0.5f
private const val interpolatedScaleAspectRatio = 1.0f
private val strides = intArrayOf(8, 16, 16, 16)

/**
 *
 */
internal data class Anchor(var xCenter: Float, var yCenter: Float)

/**
 * Generate anchors for SSD object detection model
 * Trimmed version of https://github.com/google/mediapipe/blob/master/mediapipe/calculators/tflite/ssd_anchors_calculator.cc
 * @param size input image width and height
 */
internal fun generateFaceAnchors(size: Size): List<Anchor> {
    var layerId = 0
    val anchors = mutableListOf<Anchor>()

    while (layerId < numberOfLayers) {
        var lastSameStrideLayer = layerId

        var repeats = 0
        while (lastSameStrideLayer < strides.size && strides[lastSameStrideLayer] == strides[layerId]) {
            lastSameStrideLayer++

            if (interpolatedScaleAspectRatio == 1.0f) {
                repeats += 2
            } else {
                repeats++
            }
        }
        val stride = strides[layerId]
        val featureMapHeight = size.height / stride
        val featureMapWidth = size.width / stride

        for (y in 0 until featureMapHeight) {
            val yCenter = (y + anchorOffsetY) / featureMapHeight

            for (x in 0 until featureMapWidth) {
                val xCenter = (x + anchorOffsetX) / featureMapWidth

                for (repeat in 0 until repeats) {
                    anchors.add(Anchor(xCenter, yCenter))
                }
            }
        }
        layerId = lastSameStrideLayer
    }
    return anchors
}