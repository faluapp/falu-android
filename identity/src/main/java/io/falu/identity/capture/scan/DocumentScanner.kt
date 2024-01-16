package io.falu.identity.capture.scan

import android.renderscript.RenderScript
import io.falu.identity.ai.DocumentDetectionAnalyzer
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import io.falu.identity.scan.AbstractScanner
import io.falu.identity.scan.IdentityResult
import io.falu.identity.scan.ProvisionalResult
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.scan.ScanResultCallback
import io.falu.identity.utils.toFraction
import org.joda.time.DateTime
import java.io.File

internal class DocumentScanner(
    private val model: File,
    private val threshold: Float,
    private val performanceMonitor: ModelPerformanceMonitor,
    callbacks: ScanResultCallback<ProvisionalResult, IdentityResult>
) : AbstractScanner(callbacks) {

    override fun scan(
        view: CameraView,
        scanType: ScanDisposition.DocumentScanType,
        capture: VerificationCapture,
        renderScript: RenderScript
    ) {
        val machine = DocumentDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout),
            iou = capture.blur?.iou?.toFraction() ?: 0.95f,
            requiredTime = capture.blur?.duration?.div(1000) ?: 5
        )

        disposition = ScanDisposition.Start(scanType, machine)

        view.analyzers.add(
            DocumentDetectionAnalyzer
                .Builder(model = model, threshold, renderScript, performanceMonitor)
                .instance { onResult(it) }
        )
    }
}