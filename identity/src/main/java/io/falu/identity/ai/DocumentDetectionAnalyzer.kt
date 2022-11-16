package io.falu.identity.ai

import android.annotation.SuppressLint
import android.graphics.Bitmap
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

    private var isFrameProcessing = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        if ( isFrameProcessing ) {
            image.close()
            return
        }

        isFrameProcessing = true

        // Input
        val bitmap = image.image?.toBitmap()
        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(bitmap)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        tensorImage = processor.process(tensorImage)
        
        // run
        val boundingBoxes = Array(200) { FloatArray(BOUNDING_BOX_TENSOR_SIZE) }
        val documentOptions = Array(200) { FloatArray(DOCUMENT_CATEGORY_TENSOR_SIZE) }

        interpreter.runForMultipleInputsOutputs(
            arrayOf(tensorImage.buffer), mapOf(
                BOUNDING_BOX_TENSOR_INDEX to boundingBoxes,
                CATEGORY_TENSOR_INDEX to documentOptions
            )
        )

        // Process results:
        // Find the highest score and return the corresponding box and document option

        var bestIndex = 0
        var bestScore = Float.MIN_VALUE
        var bestDocumentOptionIndex = 0

        for (score in 0 until 200) {
            val currentScores = documentOptions[score]
            val currentBestDocumentOptionIndex = currentScores.indices.maxBy { currentScores[it] }

            val best = currentScores[bestDocumentOptionIndex]

            // TODO : Set the threshold score
            if (bestScore < best) {
                bestScore = best
                bestIndex = score
                bestDocumentOptionIndex = currentBestDocumentOptionIndex
            }
        }

        val bestBoundingBox = boundingBoxes[bestIndex]

        val output = DocumentDetectionOutput(
            box = BoundingBox(
                bestBoundingBox[0],
                bestBoundingBox[1],
                bestBoundingBox[2],
                bestBoundingBox[3]
            ),
            score = bestScore,
            scores = mutableListOf()
        )

        print(output)
    }

    internal companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32
        private val DOCUMENT_CATEGORY_TENSOR_SIZE = DocumentCategory.values().size

        private const val IMAGE_WIDTH = 224
        private const val IMAGE_HEIGHT = 224
        private const val BOUNDING_BOX_TENSOR_SIZE = 4
        private const val BOUNDING_BOX_TENSOR_INDEX = 0
        private const val CATEGORY_TENSOR_INDEX = 1

        private const val HUDUMA_BACK = "huduma_back"
        private const val HUDUMA_FRONT = "huduma_front"
        private const val KENYA_DL_BACK = "kenya_dl_back"
        private const val KENYA_DL_FRONT = "kenya_dl_front"
        private const val KENYA_ID_BACK = "kenya_id_back"
        private const val KENYA_ID_FRONT = "kenya_id_front"
        private const val DOCUMENT_OPTION_INVALID = "invalid"

        private val DOCUMENT_OPTIONS = listOf(
            HUDUMA_BACK,
            HUDUMA_FRONT,
            KENYA_DL_BACK,
            KENYA_DL_FRONT,
            KENYA_ID_BACK,
            KENYA_ID_FRONT,
            DOCUMENT_OPTION_INVALID
        )

        private val DOCUMENT_CATEGORY_MAP = mapOf(
            HUDUMA_BACK to DocumentCategory.HUDAMA_BACK,
            HUDUMA_FRONT to DocumentCategory.HUDAMA_FRONT,
            KENYA_DL_BACK to DocumentCategory.KENYA_DL_BACK,
            KENYA_DL_FRONT to DocumentCategory.KENYA_DL_FRONT,
            KENYA_ID_BACK to DocumentCategory.KENYA_ID_BACK,
            KENYA_ID_FRONT to DocumentCategory.KENYA_DL_FRONT,
            DOCUMENT_OPTION_INVALID to DocumentCategory.INVALID
        )
    }
}