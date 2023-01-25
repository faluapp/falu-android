package io.falu.identity.ai

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.camera.AnalyzerBuilder
import io.falu.identity.camera.AnalyzerOutputListener
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.utils.*
import io.falu.identity.utils.toBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import kotlin.math.max
import kotlin.math.min

internal class DocumentDetectionAnalyzer internal constructor(
    model: File,
    private val threshold: Float,
    private val listener: AnalyzerOutputListener
) :
    ImageAnalysis.Analyzer {

    private val interpreter = Interpreter(model)

    private val maxDetections = 10
    private val boundingBoxesTensorShape = intArrayOf(1, maxDetections, 4)
    private val scoresTensorShape = intArrayOf(1, maxDetections)
    private val classesTensorShape = intArrayOf(1, maxDetections)
    private val detectionsTensorShape = intArrayOf(1)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        interpreter.resetVariableTensors()

        // Input:- [1,320,320,1]
        val bitmap = image.image!!.toBitmap().rotate()
        val cropped = bitmap.centerCrop(bitmap.toSize())

        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(cropped)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD)) // normalize to [0, 1)
            .build()
        tensorImage = processor.process(tensorImage)

        // run:- input: [1,320,320,1], output: (1,10), (1, 10, 4), (1,), (1,10)
        // The output is an array representing the probability scores of the various document types
        val documentOptionScoresBuffer =
            TensorBuffer.createFixedSize(scoresTensorShape, DataType.FLOAT32)
        val boundingBoxesBuffer =
            TensorBuffer.createFixedSize(boundingBoxesTensorShape, DataType.FLOAT32)
        val classesBuffer =
            TensorBuffer.createFixedSize(classesTensorShape, DataType.FLOAT32)
        val detectionsBuffer = TensorBuffer.createFixedSize(detectionsTensorShape, DataType.FLOAT32)

        interpreter.runForMultipleInputsOutputs(
            arrayOf(tensorImage.buffer), mapOf(
                0 to documentOptionScoresBuffer.buffer,
                1 to boundingBoxesBuffer.buffer,
                2 to detectionsBuffer.buffer,
                3 to classesBuffer.buffer
            )
        )

        val scores = documentOptionScoresBuffer.floatArray
        val boxes = boundingBoxesBuffer.floatArray
        val classes = classesBuffer.floatArray

        // Process results:
        // Find the highest score and return the corresponding box and document option
        var bestIndex = 0
        var bestScore = Float.MIN_VALUE
        var bestOptionIndex = INVALID

        for (i in boxes.indices step 4) {
            val currentDocumentScore = scores[i / 4]
            val currentBestClass = classes[i / 4].toInt()

            if (bestScore < currentDocumentScore && currentDocumentScore > threshold) {
                bestScore = currentDocumentScore
                bestIndex = i
                bestOptionIndex = currentBestClass
            }
        }

        val bestOption = DOCUMENT_OPTIONS_MAP[bestOptionIndex] ?: DocumentOption.INVALID
        val bestBox = boxes.sliceArray(bestIndex..bestIndex + 3)
        val box = BoundingBox(
            left = bestBox[0],// x-min
            top = bestBox[1], // y-min
            width = bestBox[2] - bestBox[0], // x-max - x-min
            height = bestBox[3] - bestBox[1] // y-max - y-min
        )

        val output = DocumentDetectionOutput(
            score = bestScore,
            option = bestOption,
            bitmap = cropped,
            box = box,
            rect = getRect(bestBox, cropped),
            scores = DOCUMENT_OPTIONS.map { scores[bestIndex] }.toMutableList()
        )

        listener(output)

        image.close()
    }

    private fun getRect(coordinates: FloatArray, bitmap: Bitmap): Rect {
        val xMin = coordinates[0] * bitmap.width
        val yMin = coordinates[1] * bitmap.height
        val xMax = coordinates[2] * bitmap.width
        val yMax = coordinates[3] * bitmap.height

        val width = xMax - xMin
        val height = yMax - yMin

        return Rect(
            max(xMin.toInt(), 1),
            max(yMin.toInt(), 1),
            min(width.toInt(), bitmap.width),
            min(height.toInt(), bitmap.height)
        )
    }

    internal class Builder(private val model: File, private val threshold: Float) :
        AnalyzerBuilder<DocumentScanDisposition, DetectionOutput, ImageAnalysis.Analyzer> {

        override fun instance(result: (DetectionOutput) -> Unit): ImageAnalysis.Analyzer {
            return DocumentDetectionAnalyzer(model, threshold, result)
        }
    }

    internal companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32

        private const val IMAGE_WIDTH = 320
        private const val IMAGE_HEIGHT = 320
        private const val NORMALIZE_MEAN = 0f
        private const val NORMALIZE_STD = 255f

        private const val INVALID = -1
        private const val PASSPORT = 0
        private const val DL_BACK = 1
        private const val DL_FRONT = 2
        private const val ID_BACK = 3
        private const val ID_FRONT = 4

        private val DOCUMENT_OPTIONS = listOf(
            DL_BACK,
            DL_FRONT,
            ID_BACK,
            ID_FRONT,
            PASSPORT,
        )

        private val DOCUMENT_OPTIONS_MAP = mapOf(
            DL_BACK to DocumentOption.DL_BACK,
            DL_FRONT to DocumentOption.DL_FRONT,
            ID_BACK to DocumentOption.ID_BACK,
            ID_FRONT to DocumentOption.ID_FRONT,
            PASSPORT to DocumentOption.PASSPORT
        )
    }
}