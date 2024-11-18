package io.falu.identity.scan

import android.content.Context
import io.falu.identity.ai.FaceDetectionAnalyzer
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.utils.getRenderScript
import java.io.File

internal class FaceScanner(private val context: Context) : AbstractScanner() {

    override fun addAnalyzers(
        model: File,
        capture: VerificationCapture,
        scanType: ScanDisposition.DocumentScanType,
        performanceMonitor: ModelPerformanceMonitor
    ) {
        requireCameraView().analyzers.add(
            FaceDetectionAnalyzer.Builder(
                model = model,
                performanceMonitor,
                capture.models.face?.threshold ?: 0.75f,
                context.getRenderScript()
            ).instance { onResult(it) }
        )
    }

    override fun onCameraViewReady() {
        // requireCameraView().ivCameraBorder.setBackgroundResource(R.drawable.ic_falu_selfie_border)
    }
}