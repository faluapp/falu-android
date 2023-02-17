import androidx.camera.core.ImageAnalysis
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionAnalyzer
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentOption
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import io.falu.identity.capture.scan.utils.DocumentDispositionMachine
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.capture.scan.utils.DocumentScanResultCallback
import io.falu.identity.capture.scan.utils.ScanResult
import io.falu.identity.utils.toFraction
import org.joda.time.DateTime
import java.io.File

internal class DocumentScanner(
    private val model: File,
    private val threshold: Float,
    private val callback: DocumentScanResultCallback<ScanResult>
) {
    private var disposition: DocumentScanDisposition? = null
    private var isFirstOutput = false

    internal fun scan(
        view: CameraView,
        scanType: DocumentScanDisposition.DocumentScanType,
        capture: VerificationCapture
    ) {
        val machine = DocumentDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout),
            iou = capture.blur?.iou?.toFraction() ?: 0.95f,
            requiredTime = capture.blur?.duration?.div(1000) ?: 5
        )

        disposition = DocumentScanDisposition.Start(scanType, machine)

        view.analyzers.add(
            DocumentDetectionAnalyzer
                .Builder(model = model, threshold)
                .instance { handleResult(it, view) }
        )
    }

    fun stopScan(view: CameraView) {
        view.analyzers.clear()
        view.stopAnalyzer()
    }

    private fun handleResult(output: DetectionOutput, view: CameraView) {
        requireNotNull(disposition) { "Initial Disposition cannot be null" }

        if (isFirstOutput) {
            val previousDisposition = disposition!!
            disposition = previousDisposition.next(output)

            val result = ScanResult(output, disposition)

            if (disposition!!.terminate) {
                stopScan(view)
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