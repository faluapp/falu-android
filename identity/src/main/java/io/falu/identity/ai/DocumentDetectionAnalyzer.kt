package io.falu.identity.ai

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.utils.toBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File

internal class DocumentDetectionAnalyzer internal constructor(model: File) :
    ImageAnalysis.Analyzer {

    private val interpreter = Interpreter(model)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        // Input:- [1,1,1,3]
        val bitmap = image.image?.toBitmap()
        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(bitmap)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        tensorImage = processor.process(tensorImage)

        // run:- input: [1,1,1,3], output: (1,6)
        // The output is an array representing the probability scores of the various document types
        val documentOptionScores = Array(OUTPUT_SIZE) { FloatArray(DOCUMENT_OPTION_TENSOR_SIZE) }

        interpreter.run(tensorImage.buffer, documentOptionScores)

        // Process results:
        // Find the highest score and return the corresponding box and document option
        var bestIndex = 0
        var bestScore = 0F
        var bestOptionIndex = INVALID

        for (score in 0 until OUTPUT_SIZE) {
            val currentDocumentScores = documentOptionScores[score]
            val currentBestDocumentOptionIndex =
                currentDocumentScores.indices.maxBy { currentDocumentScores[it] }

            val currentBestOptionScore = currentDocumentScores[currentBestDocumentOptionIndex]

            if (bestScore < currentBestOptionScore && currentBestOptionScore > 0.8) {
                bestScore = currentBestOptionScore
                bestIndex = score
                bestOptionIndex = currentBestDocumentOptionIndex
            }
        }

        val bestOption = DOCUMENT_OPTIONS_MAP[bestOptionIndex] ?: DocumentOption.INVALID

        val output = DocumentDetectionOutput(
            score = bestScore,
            option = bestOption,
            scores = DOCUMENT_OPTIONS.map { documentOptionScores[bestIndex][it] }.toMutableList()
        )


        // TODO: 2022-11-17 Return result.
        image.close()
    }

    internal companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32
        private val DOCUMENT_OPTION_TENSOR_SIZE = DocumentOption.values().size - 1

        private const val IMAGE_WIDTH = 224
        private const val IMAGE_HEIGHT = 224
        private const val OUTPUT_SIZE = 1

        private const val HUDUMA_BACK = 0
        private const val HUDUMA_FRONT = 1
        private const val KENYA_DL_BACK = 2
        private const val KENYA_DL_FRONT = 3
        private const val KENYA_ID_BACK = 4
        private const val KENYA_ID_FRONT = 5
        private const val INVALID = -1

        private val DOCUMENT_OPTIONS = listOf(
            HUDUMA_BACK,
            HUDUMA_FRONT,
            KENYA_DL_BACK,
            KENYA_DL_FRONT,
            KENYA_ID_BACK,
            KENYA_ID_FRONT,
        )

        private val DOCUMENT_OPTIONS_MAP = mapOf(
            HUDUMA_BACK to DocumentOption.HUDAMA_BACK,
            HUDUMA_FRONT to DocumentOption.HUDAMA_FRONT,
            KENYA_DL_BACK to DocumentOption.KENYA_DL_BACK,
            KENYA_DL_FRONT to DocumentOption.KENYA_DL_FRONT,
            KENYA_ID_BACK to DocumentOption.KENYA_ID_BACK,
            KENYA_ID_FRONT to DocumentOption.KENYA_DL_FRONT,
        )
    }
}