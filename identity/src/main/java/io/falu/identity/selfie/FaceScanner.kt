package io.falu.identity.selfie

import io.falu.identity.ai.FaceDetectionAnalyzer
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import io.falu.identity.scan.*
import org.joda.time.DateTime
import java.io.File

internal class FaceScanner(
    private val model: File,
    private val threshold: Float,
    callback: ScanResultCallback<ProvisionalResult, IdentityResult>
) : AbstractScanner(callback) {

    override fun scan(
        view: CameraView,
        scanType: ScanDisposition.DocumentScanType,
        capture: VerificationCapture
    ) {
        val machine = FaceDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout)
        )

        disposition =
            ScanDisposition.Start(scanType, machine)

        view.analyzers.add(
            FaceDetectionAnalyzer
                .Builder(model = model, threshold)
                .instance { onResult(it) }
        )
    }
}