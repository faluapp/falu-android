import android.content.Context
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
    private val callbacks: DocumentScanResultCallback<ProvisionalResult, IdentityResult>
) {
    private var disposition: DocumentScanDisposition? = null
    private var isFirstOutput = false

    /**
     *
     */
    internal data class ProvisionalResult(val disposition: DocumentScanDisposition)

    /**
     *
     */
    internal data class IdentityResult(
        val output: DetectionOutput,
        val disposition: DocumentScanDisposition
    )

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

        val (provisionalResult, identityResult) = collectResults(output)

        callbacks.onProgress(provisionalResult)

        identityResult?.also {
            callbacks.onScanComplete(it)
        }
    }

    private fun collectResults(output: DetectionOutput): Pair<ProvisionalResult, IdentityResult?> {
        return if (isFirstOutput) {
            val previousDisposition = disposition!!
            disposition = previousDisposition.next(output)
            val provisionalResult = ProvisionalResult(disposition!!)

            provisionalResult to
                    if (disposition!!.terminate) {
                        IdentityResult(output, disposition!!)
                    } else {
                        null
                    }
        } else {
            isFirstOutput = true
            ProvisionalResult(disposition!!) to null
        }
    }
}