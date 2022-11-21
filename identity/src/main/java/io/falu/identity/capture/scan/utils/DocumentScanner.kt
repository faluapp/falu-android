import androidx.camera.core.ImageAnalysis
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionAnalyzer
import io.falu.identity.capture.scan.DocumentScanResultCallback
import io.falu.identity.capture.scan.utils.DocumentDispositionChanger
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import java.io.File

internal class DocumentScanner(
    private val model: File,
    private val threshold: Float,
    private val callback: DocumentScanResultCallback<DocumentScanDisposition, DetectionOutput>
) {
    private var disposition: DocumentScanDisposition? = null
    private var isFirstOutput = false

    internal fun scan(
        analyzers: MutableList<ImageAnalysis.Analyzer>,
        scanType: DocumentScanDisposition.DocumentScanType
    ) {
        disposition = DocumentScanDisposition.Start(scanType, DocumentDispositionChanger())

        analyzers.add(
            DocumentDetectionAnalyzer
                .Builder(model = model, threshold)
                .instance { handleResult(it) }
        )
    }

    private fun handleResult(output: DetectionOutput) {
        requireNotNull(disposition) { "Initial Disposition cannot be null" }

        if (isFirstOutput) {
            val previousDisposition = disposition!!
            disposition = previousDisposition.next(output)

            if (disposition is DocumentScanDisposition.Completed) {
                callback.onScanComplete(output)
            } else {
                callback.onProgress(disposition!!)
            }
        } else {
            isFirstOutput = true
            callback.onProgress(disposition!!)
        }
    }
}