package io.falu.identity.scan

import io.falu.identity.ai.DetectionOutput
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import java.io.File

internal abstract class AbstractScanner {
    internal lateinit var callbacks: ScanResultCallback<ProvisionalResult, IdentityResult>

    internal var disposition: ScanDisposition? = null
    private var cameraView: CameraView? = null

    private var previousDisposition: ScanDisposition? = null

    private var isFirstOutput = false

    internal fun onUpdateCameraView(view: CameraView) {
        if (cameraView == null) {
            cameraView = view
            onCameraViewReady()
        }
    }

    protected open fun onCameraViewReady() {}

    internal abstract fun addAnalyzers(
        model: File,
        capture: VerificationCapture,
        scanType: ScanDisposition.DocumentScanType,
        performanceMonitor: ModelPerformanceMonitor,
    )

    fun requireCameraView() = requireNotNull(cameraView)

    internal fun onResult(output: DetectionOutput) {

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