package io.falu.identity.ai

import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File

internal class FaceDetectionAnalyzer internal constructor(model: File) {

    private val interpreter = Interpreter(model)

    fun analyze(bitmap: Bitmap): FaceDetectionOutput {
        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(bitmap)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD)) // normalize to [0, 1)
            .build()
        tensorImage = processor.process(tensorImage)

        val boxes = Array(1) { FloatArray(BOUNDING_BOX_TENSOR_SIZE) }
        val score = FloatArray(SCORE_TENSOR_SIZE)

        interpreter.runForMultipleInputsOutputs(
            arrayOf(tensorImage.buffer),
            mapOf(
                BOUNDING_BOX_TENSOR_SIZE to boxes,
                SCORE_TENSOR_SIZE to score
            )
        )

        return FaceDetectionOutput(score = score[0])
    }

    companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32

        private const val IMAGE_WIDTH = 128
        private const val IMAGE_HEIGHT = 128
        private const val NORMALIZE_MEAN = 0f
        private const val NORMALIZE_STD = 255f

        const val BOUNDING_BOX_TENSOR_SIZE = 4
        const val SCORE_TENSOR_SIZE = 1
    }
}