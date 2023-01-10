package io.falu.identity.ai

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.camera.AnalyzerBuilder
import io.falu.identity.camera.AnalyzerOutputListener
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.utils.toBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File

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
        val bitmap = image.image?.toBitmap()
        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(bitmap)

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

        val output = DocumentDetectionOutput(
            score = bestScore,
            option = bestOption,
            bitmap = bitmap!!,
            box = BoundingBox(
                left = bestBox[0],
                top = bestBox[1],
                width = bestBox[2],
                height = bestBox[3]
            ),
            scores = DOCUMENT_OPTIONS.map { scores[bestIndex] }.toMutableList()
        )

        listener(output)

        image.close()
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

        private const val INVALID = 0
        private const val KENYA_PASSPORT = 1
        private const val KENYA_DL_BACK = 2
        private const val KENYA_DL_FRONT = 3
        private const val KENYA_ID_BACK = 4
        private const val KENYA_ID_FRONT = 5

        private val DOCUMENT_OPTIONS = listOf(
            KENYA_DL_BACK,
            KENYA_DL_FRONT,
            KENYA_ID_BACK,
            KENYA_ID_FRONT,
            KENYA_PASSPORT
        )

        private val DOCUMENT_OPTIONS_MAP = mapOf(
            KENYA_DL_BACK to DocumentOption.KENYA_DL_BACK,
            KENYA_DL_FRONT to DocumentOption.KENYA_DL_FRONT,
            KENYA_ID_BACK to DocumentOption.KENYA_ID_BACK,
            KENYA_ID_FRONT to DocumentOption.KENYA_ID_FRONT,
            KENYA_PASSPORT to DocumentOption.KENYA_PASSPORT
        )
    }
}