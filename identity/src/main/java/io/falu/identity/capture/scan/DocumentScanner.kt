package io.falu.identity.capture.scan

import android.content.Context
import io.falu.identity.R
import io.falu.identity.ai.DocumentDetectionAnalyzer
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.scan.AbstractScanner
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.utils.getRenderScript
import io.falu.identity.utils.toFraction
import org.joda.time.DateTime
import java.io.File

internal class DocumentScanner(private val context: Context) : AbstractScanner() {

    override fun onCameraViewReady() {
        requireCameraView().ivCameraBorder.setBackgroundResource(R.drawable.ic_falu_document_border)
    }

    override fun addAnalyzers(
        model: File,
        capture: VerificationCapture,
        scanType: ScanDisposition.DocumentScanType,
        performanceMonitor: ModelPerformanceMonitor
    ) {
        val machine = DocumentDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout),
            iou = capture.blur?.iou?.toFraction() ?: 0.95f,
            requiredTime = capture.blur?.duration?.div(1000) ?: 5
        )

        disposition = ScanDisposition.Start(scanType, machine)

        requireCameraView().analyzers.add(
            DocumentDetectionAnalyzer.Builder(
                model = model,
                capture.models.document.threshold,
                context.getRenderScript(),
                performanceMonitor
            ).instance { onResult(it) }
        )
    }
}