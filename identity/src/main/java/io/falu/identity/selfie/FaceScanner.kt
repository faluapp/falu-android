package io.falu.identity.selfie

import androidx.camera.core.ImageAnalysis
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.FaceDetectionAnalyzer
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.capture.scan.utils.DocumentScanResultCallback
import io.falu.identity.capture.scan.utils.ScanResult
import org.joda.time.DateTime
import java.io.File

internal class FaceScanner(
    private val model: File,
    private val threshold: Float,
    private val callback: DocumentScanResultCallback<ScanResult>
) {
    private var disposition: DocumentScanDisposition? = null
    private var isFirstOutput = false

    internal fun scan(
        analyzers: MutableList<ImageAnalysis.Analyzer>,
        capture: VerificationCapture
    ) {
        val machine = FaceDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout)
        )

        disposition =
            DocumentScanDisposition.Start(DocumentScanDisposition.DocumentScanType.SELFIE, machine)

        analyzers.add(
            FaceDetectionAnalyzer
                .Builder(model = model, threshold)
                .instance { handleResult(it) }
        )
    }

    private fun handleResult(output: DetectionOutput) {
        requireNotNull(disposition) { "Initial Disposition cannot be null" }

        if (isFirstOutput) {
            val previousDisposition = disposition!!
            disposition = previousDisposition.next(output)

            val result = ScanResult(output, disposition)

            if (disposition!!.terminate) {
                callback.onScanComplete(result)
            } else {
                callback.onProgress(result)
            }

        } else {
            val result = ScanResult(output, disposition)

            isFirstOutput = true
            callback.onProgress(result)
        }
    }
}