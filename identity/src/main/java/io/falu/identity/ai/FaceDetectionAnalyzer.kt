package io.falu.identity.ai

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.Interpreter
import java.io.File

internal class FaceDetectionAnalyzer internal constructor(model: File) :
    ImageAnalysis.Analyzer {

    private val interpreter = Interpreter(model)

    override fun analyze(image: ImageProxy) {
        interpreter.allocateTensors()
    }

}