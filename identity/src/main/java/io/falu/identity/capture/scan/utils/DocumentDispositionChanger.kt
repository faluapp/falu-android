package io.falu.identity.capture.scan.utils

import android.util.Log
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentOption


internal class DocumentDispositionChanger : DocumentDispositionDetector {
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
        return DocumentScanDisposition.Completed(state.type, this)
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
    }
}