package io.falu.identity.scan

import android.renderscript.RenderScript
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView

internal abstract class AbstractScanner(
    private val callbacks: ScanResultCallback<ProvisionalResult, IdentityResult>
) {

    protected var disposition: ScanDisposition? = null
    private var previousDisposition: ScanDisposition? = null

    private var isFirstOutput = false

    internal abstract fun scan(
        view: CameraView,
        scanType: ScanDisposition.DocumentScanType,
        capture: VerificationCapture,
        renderScript: RenderScript
    )

    internal fun stopScan(view: CameraView) {
        view.analyzers.clear()
        view.stopAnalyzer()
    }

    internal fun onResult(output: DetectionOutput) {
        requireNotNull(disposition) { "Initial Disposition cannot be null" }

        val (provisionalResult, identityResult) = collectResults(output)

        callbacks.onProgress(provisionalResult)

        identityResult?.also {
            callbacks.onScanComplete(it)
        }
    }

    internal fun changeDisposition(
        new: ScanDisposition,
        change: ((Boolean) -> Unit)
    ) {
        if (disposition == previousDisposition && previousDisposition?.terminate == true) {
            change(false)
            return
        }

        disposition = new
        change(true)
        previousDisposition = disposition
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