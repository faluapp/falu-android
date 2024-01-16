package io.falu.identity.selfie

import android.renderscript.RenderScript
import io.falu.identity.ai.FaceDetectionAnalyzer
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import io.falu.identity.scan.AbstractScanner
import io.falu.identity.scan.IdentityResult
import io.falu.identity.scan.ProvisionalResult
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.scan.ScanResultCallback
import org.joda.time.DateTime
import java.io.File

internal class FaceScanner(
    private val model: File,
    private val threshold: Float,
    private val performanceMonitor: ModelPerformanceMonitor,
    callback: ScanResultCallback<ProvisionalResult, IdentityResult>
) : AbstractScanner(callback) {

    override fun scan(
        view: CameraView,
        scanType: ScanDisposition.DocumentScanType,
        capture: VerificationCapture,
        renderScript: RenderScript
    ) {
        val machine = FaceDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout)
        )

        disposition =
            ScanDisposition.Start(scanType, machine)

        view.analyzers.add(
            FaceDetectionAnalyzer
                .Builder(model = model, performanceMonitor, threshold, renderScript)
                .instance { onResult(it) }
        )
    }
}