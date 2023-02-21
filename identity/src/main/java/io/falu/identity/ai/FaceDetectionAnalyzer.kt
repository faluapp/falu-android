package io.falu.identity.ai

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.camera.AnalyzerBuilder
import io.falu.identity.camera.AnalyzerOutputListener
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.utils.*
import io.falu.identity.utils.centerCrop
import io.falu.identity.utils.maxAspectRatio
import io.falu.identity.utils.rotate
import io.falu.identity.utils.toBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import kotlin.math.*

internal class FaceDetectionAnalyzer internal constructor(
    model: File,
    private val threshold: Float,
    private val listener: AnalyzerOutputListener
) : ImageAnalysis.Analyzer {

    private val interpreter = Interpreter(model)

    private val regressTensorShape = intArrayOf(1, OUTPUT_SIZE, 16)
    private val classifiersTensorShape = intArrayOf(1, OUTPUT_SIZE, 3)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        interpreter.resetVariableTensors()

        // Input:- [1,128,128,3]
        val bitmap = image.image!!.toBitmap().rotate(image.imageInfo.rotationDegrees)

        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(bitmap)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD)) // normalize to [-1, 1)
            .build()
        tensorImage = processor.process(tensorImage)

        val regressBuffer =
            TensorBuffer.createFixedSize(regressTensorShape, DataType.FLOAT32)
        val classifiersBuffer =
            TensorBuffer.createFixedSize(classifiersTensorShape, DataType.FLOAT32)

        interpreter.runForMultipleInputsOutputs(
            arrayOf(tensorImage.buffer),
            mapOf(
                0 to regressBuffer.buffer,
                1 to classifiersBuffer.buffer,
            )
        )

        val boxes = regressBuffer.floatArray
        val scores = classifiersBuffer.floatArray

        var bestScore = Float.MIN_VALUE
        var bestBoxYMin = Float.MIN_VALUE
        var bestBoxXMin = Float.MIN_VALUE
        var bestBoxYMax = Float.MIN_VALUE
        var bestBoxXMax = Float.MIN_VALUE

        for (currentBoxOutputIndex in 0 until OUTPUT_SIZE) {
            val offset = 0

            val decodedBoxes =
                decodeBoundingBox(boxes, currentBoxOutputIndex, IMAGE_WIDTH, IMAGE_HEIGHT)
            val yMin = decodedBoxes[offset + 0]
            val xMin = decodedBoxes[offset + 1]
            val yMax = decodedBoxes[offset + 2]
            val xMax = decodedBoxes[offset + 3]

            for (scoreIndex in 0..NUMBER_OF_CLASSES) {
                var currentScore =
                    scores[currentBoxOutputIndex * NUMBER_OF_CLASSES + scoreIndex].toDouble()

                if (currentScore < -SCORE_CLIPPING_THRESHOLD) {
                    currentScore = -SCORE_CLIPPING_THRESHOLD
                }
                if (currentScore > SCORE_CLIPPING_THRESHOLD) {
                    currentScore = SCORE_CLIPPING_THRESHOLD
                }

                currentScore = 1.0 / (1.0 + exp(-currentScore))

                if (bestScore < currentScore && currentScore > threshold) {
                    bestScore = currentScore.toFloat()
                    bestBoxYMin = yMin
                    bestBoxXMin = xMin
                    bestBoxYMax = yMax
                    bestBoxXMax = xMax
                }
            }
        }

        val box = BoundingBox(
            left = bestBoxXMin,// x-min
            top = bestBoxYMin, // y-min
            width = bestBoxXMax - bestBoxXMin, // x-max - x-min
            height = bestBoxYMax - bestBoxYMin // y-max - y-min
        )

        val x1 = bitmap.width * bestBoxXMin
        val x2 = bitmap.width * (bestBoxXMin + box.width)
        val y1 = bitmap.height * bestBoxYMin
        val y2 = bitmap.height * (bestBoxYMin + box.height)

        val rect = Rect(
            max(x1.toInt(), 1),
            max(y1.toInt(), 1),
            min(x2.toInt(), bitmap.width),
            min(y2.toInt(), bitmap.height)
        )

        val output = FaceDetectionOutput(
            score = bestScore,
            bitmap = bitmap,
            box = box,
            rect = rect
        )

        listener(output)

        image.close()
    }

    private fun getAnchors(width: Int, height: Int): List<Anchor> {
        val strides = intArrayOf(8, 16, 16, 16)
        val layers = 4
        val maxScale = 0.75
        val minScale = 0.1484375
        val reduceBoxesInLowestLayer = false
        val ratios = listOf(0.1f)
        val interpolatedScaleAspectRatio = 1.0f
        val featureMapHeights = mutableListOf<Int>()
        val featureMapWidths = mutableListOf<Int>()
        val anchorOffsetX = 0.5f
        val anchorOffsetY = 0.5f
        val fixedAnchorSize = true
        val anchors = mutableListOf<Anchor>()

        if (strides.size != layers) {
            return emptyList()
        }

        var layerId = 0
        while (layerId < strides.size) {
            val anchorHeights = mutableListOf<Float>()
            val anchorWidths = mutableListOf<Float>()
            val aspectRatios = mutableListOf<Float>()
            val scales = mutableListOf<Float>()

            var lastSameStrideLayer = layerId

            while (lastSameStrideLayer < strides.size &&
                strides[lastSameStrideLayer] == strides[layerId]
            ) {
                val scale = minScale +
                        (maxScale - minScale) *
                        1.0 *
                        lastSameStrideLayer /
                        (strides.size - 1.0)

                if (lastSameStrideLayer == 0 && reduceBoxesInLowestLayer) {
                    aspectRatios.add(1.0f);
                    aspectRatios.add(2.0f);
                    aspectRatios.add(0.5f);
                    scales.add(0.1f)
                    scales.add(scale.toFloat())
                    scales.add(scale.toFloat())
                } else {
                    for (i in ratios.indices) {
                        aspectRatios.add(ratios[i])
                        scales.add(scale.toFloat())
                    }

                    if (interpolatedScaleAspectRatio > 0.0) {
                        val scaleNext: Float = if (lastSameStrideLayer == strides.size - 1) {
                            1.0f
                        } else {
                            (minScale +
                                    (maxScale - minScale) *
                                    1.0 *
                                    (lastSameStrideLayer + 1) /
                                    (strides.size - 1.0)).toFloat()
                        }

                        scales.add(sqrt(scale * scaleNext).toFloat())
                        aspectRatios.add(interpolatedScaleAspectRatio);
                    }
                }

                lastSameStrideLayer++
            }

            for (i in 0 until aspectRatios.size) {
                val ratioSQRT = sqrt(aspectRatios[i])
                anchorHeights.add(scales[i] / ratioSQRT)
                anchorWidths.add(scales[i] * ratioSQRT)
            }

            var featureMapHeight = 0
            var featureMapWidth = 0
            if (featureMapHeights.size > 0) {
                featureMapHeight = featureMapHeights[layerId]
                featureMapWidth = featureMapWidths[layerId]
            } else {
                val stride: Int = strides[layerId]
                featureMapHeight = ceil((1.0 * height / stride)).toInt()
                featureMapWidth = ceil((1.0 * width / stride)).toInt()
            }

            for (y in 0 until featureMapHeight) {
                for (x in 0 until featureMapWidth) {
                    for (anchorID in 0 until anchorHeights.size) {
                        val xCenter: Double =
                            (x + anchorOffsetX) * 1.0 / featureMapWidth
                        val yCenter: Double =
                            (y + anchorOffsetY) * 1.0 / featureMapHeight
                        var w: Float
                        var h: Float
                        if (fixedAnchorSize) {
                            w = 1.0f
                            h = 1.0f
                        } else {
                            w = anchorWidths[anchorID]
                            h = anchorHeights[anchorID]
                        }
                        anchors.add(
                            Anchor(
                                xCenter.toFloat(),
                                yCenter.toFloat(),
                                h,
                                w
                            )
                        )
                    }
                }
            }
            layerId = lastSameStrideLayer
        }

        return anchors
    }

    private fun decodeBoundingBox(
        coordinates: FloatArray,
        index: Int,
        width: Int,
        height: Int
    ): List<Float> {
        val anchors = getAnchors(width, height)
        val boxCoordOffset = 0
        val numCoords = 16
        val reverseOutputOrder = false
        val boxOffset = index * numCoords + boxCoordOffset
        val xScale = 128
        val yScale = 128
        val hScale = 128
        val wScale = 128
        val numKeypoints = 6
        val keypointCoordOffset = 4
        val numValuesPerKeypoint = 2
        val applyExponentialOnBoxSize = false

        val boxData = buildList { repeat(numCoords) { add(0.0f) } }.toMutableList()

        var yCenter = coordinates[boxOffset]
        var xCenter = coordinates[boxOffset + 1]
        var h = coordinates[boxOffset + 2]
        var w = coordinates[boxOffset + 3]
        if (reverseOutputOrder) {
            xCenter = coordinates[boxOffset]
            yCenter = coordinates[boxOffset + 1]
            w = coordinates[boxOffset + 2]
            h = coordinates[boxOffset + 3]
        }

        xCenter = xCenter / xScale * anchors[index].width + anchors[index].xCenter
        yCenter = yCenter / yScale * anchors[index].height + anchors[index].yCenter

        if (applyExponentialOnBoxSize) {
            h = exp(h / hScale) * anchors[index].height
            w = exp(w / wScale) * anchors[index].width
        } else {
            h = h / hScale * anchors[index].height
            w = w / wScale * anchors[index].width
        }

        val yMin = yCenter - h / 2.0
        val xMin = xCenter - w / 2.0
        val yMax = yCenter + h / 2.0
        val xMax = xCenter + w / 2.0

        boxData[0] = yMin.toFloat()
        boxData[1] = xMin.toFloat()
        boxData[2] = yMax.toFloat()
        boxData[3] = xMax.toFloat()

        for (k in 0 until numKeypoints) {
            val offset: Int = index * numCoords +
                    keypointCoordOffset + k * numValuesPerKeypoint
            var keyPointY = coordinates[offset]
            var keyPointX = coordinates[offset + 1]
            if (reverseOutputOrder) {
                keyPointX = coordinates[offset]
                keyPointY = coordinates[offset + 1]
            }
            boxData[4 + k * numValuesPerKeypoint] =
                keyPointX / xScale * anchors[index].width + anchors[index].xCenter
            boxData[4 + k * numValuesPerKeypoint + 1] =
                keyPointY / yScale * anchors[index].height + anchors[index].yCenter
        }

        return boxData
    }

    internal class Builder(private val model: File, private val threshold: Float) :
        AnalyzerBuilder<ScanDisposition, DetectionOutput, ImageAnalysis.Analyzer> {

        override fun instance(result: (DetectionOutput) -> Unit): ImageAnalysis.Analyzer {
            return FaceDetectionAnalyzer(model, threshold, result)
        }
    }

    companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32

        private const val IMAGE_WIDTH = 128
        private const val IMAGE_HEIGHT = 128
        private const val NORMALIZE_MEAN = 127.5f
        private const val NORMALIZE_STD = 127.5f

        private const val SCORE_CLIPPING_THRESHOLD = 100.0
        private const val OUTPUT_SIZE = 896
        private const val NUMBER_OF_CLASSES = 1
    }
}