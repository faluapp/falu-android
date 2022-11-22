package io.falu.identity.capture.scan.utils

import android.util.Log
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentOption


internal class DocumentDispositionChanger(
    private val threshold: Float = THRESHOLD,
) : DocumentDispositionDetector {
    override fun fromStart(
        state: DocumentScanDisposition.Start,
        output: DetectionOutput
    ): DocumentScanDisposition {
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            output.option.matches(state.type) -> {
                Log.d(TAG, "Model output detected with score ${output.score}, moving to Detected.")
                DocumentScanDisposition.Detected(state.type, this)
            }
            else -> {
                Log.d(TAG, "Model output mismatch (${output.option}), start disposition retained.")
                state
            }
        }
    }

    override fun fromDetected(
        state: DocumentScanDisposition.Detected,
        output: DetectionOutput
    ): DocumentScanDisposition {
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }

        return when {
            output.score < THRESHOLD -> DocumentScanDisposition.Undesired(
                state.type, state.dispositionDetector
            )
            else -> {
                DocumentScanDisposition.Desired(state.type, state.dispositionDetector)
            }
        }
    }

    override fun fromDesired(
        state: DocumentScanDisposition.Desired,
        output: DetectionOutput
    ): DocumentScanDisposition {
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }
        return DocumentScanDisposition.Completed(state.type, state.dispositionDetector)
    }

    override fun fromUndesired(
        state: DocumentScanDisposition.Undesired,
        output: DetectionOutput
    ): DocumentScanDisposition {
        return DocumentScanDisposition.Start(state.type, state.dispositionDetector)
    }

    private fun DocumentOption.matches(
        type: DocumentScanDisposition.DocumentScanType
    ): Boolean {
        return when (this) {
            DocumentOption.HUDAMA_BACK -> {
                type == DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_BACK
            }
            DocumentOption.HUDAMA_FRONT -> {
                type == DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_FRONT
            }
            DocumentOption.KENYA_DL_BACK -> {
                type == DocumentScanDisposition.DocumentScanType.DL_BACK
            }
            DocumentOption.KENYA_DL_FRONT -> {
                type == DocumentScanDisposition.DocumentScanType.DL_FRONT
            }
            DocumentOption.KENYA_ID_BACK -> {
                type == DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_BACK
            }
            DocumentOption.KENYA_ID_FRONT -> {
                type == DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_FRONT
            }
            DocumentOption.INVALID -> false
        }
    }

    internal companion object {
        private val TAG = DocumentDispositionChanger::class.java.simpleName
        private const val THRESHOLD = 0.8f
    }
}