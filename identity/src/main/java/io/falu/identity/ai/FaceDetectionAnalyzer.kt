package io.falu.identity.ai

import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.util.concurrent.Flow
import kotlin.math.exp

internal class FaceDetectionAnalyzer internal constructor(
    model: File,
    private val threshold: Float
) {

    private val interpreter = Interpreter(model)

    private val regressTensorShape = intArrayOf(1, OUTPUT_SIZE, 16)
    private val classifiersTensorShape = intArrayOf(1, OUTPUT_SIZE, 3)

    fun analyze(bitmap: Bitmap): FaceDetectionOutput {
        var tensorImage = TensorImage(TENSOR_DATA_TYPE)
        tensorImage.load(bitmap)

        // Preprocess: resize image to model input
        val processor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(NormalizeOp(NORMALIZE_MEAN, NORMALIZE_STD)) // normalize to [0, 1)
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

        var bestScore = Double.MIN_VALUE

        for (currentBoxOutputIndex in 0 until OUTPUT_SIZE) {
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
                    bestScore = currentScore
                }
            }
        }

        return FaceDetectionOutput(score = bestScore.toFloat())
    }

    companion object {
        private val TENSOR_DATA_TYPE = DataType.FLOAT32

        private const val IMAGE_WIDTH = 128
        private const val IMAGE_HEIGHT = 128
        private const val NORMALIZE_MEAN = 0f
        private const val NORMALIZE_STD = 255f

        private const val SCORE_CLIPPING_THRESHOLD = 100.0
        private const val OUTPUT_SIZE = 896
        private const val NUMBER_OF_CLASSES = 1
    }
}