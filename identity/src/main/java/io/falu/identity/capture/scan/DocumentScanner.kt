package io.falu.identity.capture.scan

import io.falu.identity.ai.DocumentDetectionAnalyzer
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import io.falu.identity.scan.*
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
    callbacks: ScanResultCallback<ProvisionalResult, IdentityResult>
) : AbstractScanner(callbacks) {

    override fun scan(
        view: CameraView,
        scanType: ScanDisposition.DocumentScanType,
        capture: VerificationCapture
    ) {
        val machine = DocumentDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout),
            iou = capture.blur?.iou?.toFraction() ?: 0.95f,
            requiredTime = capture.blur?.duration?.div(1000) ?: 5
        )

        disposition = ScanDisposition.Start(scanType, machine)

        view.analyzers.add(
            DocumentDetectionAnalyzer
                .Builder(model = model, threshold)
                .instance { onResult(it) }
        )
    }
}