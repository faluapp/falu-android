package io.falu.identity.ai

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.camera.AnalyzerBuilder
import io.falu.identity.camera.AnalyzerOutputListener
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.utils.Anchor
import io.falu.identity.utils.centerCrop
import io.falu.identity.utils.generateFaceAnchors
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
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

internal class FaceDetectionAnalyzer internal constructor(
    model: File,
    private val threshold: Float,
    private val performanceMonitor: ModelPerformanceMonitor,
    private val listener: AnalyzerOutputListener
) : ImageAnalysis.Analyzer {

    private val interpreter = Interpreter(model)
    private val sigmoidScoreThreshold = ln(0.7 / (1 - 0.7))

    private val classifiersTensorShape = intArrayOf(1, OUTPUT_SIZE, 3)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val preprocessingMonitor = performanceMonitor.monitorPreProcessing()
        interpreter.resetVariableTensors()

        // Input:- [1,128,128,3]
        val bitmap = image.image!!.toBitmap().rotate(image.imageInfo.rotationDegrees)
        val size = Size(bitmap.width, bitmap.height).maxAspectRatio(0.70f)
        val cropped = bitmap.centerCrop(size)

        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(cropped)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD)) // normalize to [-1, 1)
            .build()
        tensorImage = processor.process(tensorImage)
        preprocessingMonitor.monitor()

        val inferenceMonitor = performanceMonitor.monitorInference()
        val regressors = Array(1) { Array(OUTPUT_SIZE) { FloatArray(16) } }
        val classifiersBuffer =
            TensorBuffer.createFixedSize(classifiersTensorShape, DataType.FLOAT32)

        interpreter.runForMultipleInputsOutputs(
            arrayOf(tensorImage.buffer),
            mapOf(
                0 to regressors,
                1 to classifiersBuffer.buffer
            )
        )

        inferenceMonitor.monitor()

        val scores = classifiersBuffer.floatArray
        val anchors = generateFaceAnchors(Size(IMAGE_WIDTH, IMAGE_HEIGHT))

        var bestIndex = 0
        var bestScore = Float.MIN_VALUE

        for (currentBoxOutputIndex in 0 until OUTPUT_SIZE) {
            for (scoreIndex in 0..NUMBER_OF_CLASSES) {
                var currentScore =
                    scores[currentBoxOutputIndex * NUMBER_OF_CLASSES + scoreIndex].toDouble()

                if (currentScore < sigmoidScoreThreshold) {
                    break
                }

                currentScore = 1.0 / (1.0 + exp(-currentScore))

                if (bestScore < currentScore && currentScore > threshold) {
                    bestScore = currentScore.toFloat()
                    bestIndex = currentBoxOutputIndex
                }
            }
        }

        val boxes = generateBoxes(regressors.first(), bestIndex, anchors)
        val box = BoundingBox(
            left = boxes[0], // x-min
            top = boxes[1], // y-min
            width = boxes[2] - boxes[0], // x-max - x-min
            height = boxes[3] - boxes[1] // y-max - y-min
        )

        val output = FaceDetectionOutput(
            score = bestScore,
            bitmap = cropped,
            box = box,
            rect = getRect(boxes, cropped)
        )

        listener(output)

        image.close()
    }

    private fun getRect(coordinates: Array<Float>, bitmap: Bitmap): Rect {
        val xMin = coordinates[0] * bitmap.height
        val yMin = coordinates[1] * bitmap.width
        val xMax = coordinates[2] * bitmap.height
        val yMax = coordinates[3] * bitmap.width

        return Rect(
            max(yMin.toInt(), 1),
            max(xMin.toInt(), 1),
            min(yMax.toInt(), bitmap.width),
            min(xMax.toInt(), bitmap.height)
        )
    }

    private fun generateBoxes(
        coordinates: Array<FloatArray>,
        index: Int,
        anchors: List<Anchor>
    ): Array<Float> {
        val anchor = anchors[index]
        val boxes = Array(4) { 0f }

        val sx = coordinates[index][0]
        val sy = coordinates[index][1]
        var w = coordinates[index][2]
        var h = coordinates[index][3]

        var centerX = sx + anchor.xCenter * IMAGE_WIDTH
        var centerY = sy + anchor.yCenter * IMAGE_HEIGHT

        centerX /= IMAGE_WIDTH
        centerY /= IMAGE_HEIGHT
        w /= IMAGE_WIDTH
        h /= IMAGE_HEIGHT

        boxes[0] = (centerX - w * 0.5).toFloat()
        boxes[1] = (centerY - h * 0.5).toFloat()
        boxes[2] = (centerX + w * 0.5).toFloat()
        boxes[3] = (centerY + h * 0.5).toFloat()
        return boxes
    }

    internal class Builder(
        private val model: File,
        private val monitor: ModelPerformanceMonitor,
        private val threshold: Float
    ) :
        AnalyzerBuilder<ScanDisposition, DetectionOutput, ImageAnalysis.Analyzer> {

        override fun instance(result: (DetectionOutput) -> Unit): ImageAnalysis.Analyzer {
            return FaceDetectionAnalyzer(model, threshold, monitor, result)
        }
    }

    companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32

        private const val IMAGE_WIDTH = 128
        private const val IMAGE_HEIGHT = 128
        private const val NORMALIZE_MEAN = 127.5f
        private const val NORMALIZE_STD = 127.5f

        private const val OUTPUT_SIZE = 896
        private const val NUMBER_OF_CLASSES = 1
    }
}