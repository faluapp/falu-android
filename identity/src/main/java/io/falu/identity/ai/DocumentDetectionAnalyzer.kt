package io.falu.identity.ai

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.camera.AnalyzerBuilder
import io.falu.identity.camera.AnalyzerOutputListener
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.utils.rotate
import io.falu.identity.utils.toBitmap
import java.io.File

internal class DocumentDetectionAnalyzer internal constructor(
    model: File,
    threshold: Float,
    performanceMonitor: ModelPerformanceMonitor,
    private val listener: AnalyzerOutputListener
) : ImageAnalysis.Analyzer {

    private val engine = DocumentEngine(model, threshold, performanceMonitor)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        // Input:- [1,320,320,1]
        val bitmap = image.image?.toBitmap()?.rotate(image.imageInfo.rotationDegrees)

        val output = bitmap?.let { engine.analyze(it) }

        if (output != null) {
            listener(output)
        }

        image.close()
    }

    internal class Builder(
        private val model: File,
        private val threshold: Float,
        private val performanceMonitor: ModelPerformanceMonitor
    ) :
        AnalyzerBuilder<ScanDisposition, DetectionOutput, ImageAnalysis.Analyzer> {

        override fun instance(result: (DetectionOutput) -> Unit): ImageAnalysis.Analyzer {
            return DocumentDetectionAnalyzer(model, threshold, performanceMonitor, result)
        }
    }
}