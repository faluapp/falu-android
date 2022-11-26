package io.falu.identity.capture.scan.utils

import android.util.Log
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentOption
import org.joda.time.DateTime
import org.joda.time.Seconds


internal class DocumentDispositionChanger(
    private val threshold: Float = THRESHOLD,
    private val requiredTime: Int = DEFAULT_REQUIRED_SCAN_DURATION,
    private val currentTime: DateTime = DateTime.now()
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
            output.score < threshold -> DocumentScanDisposition.Undesired(
                state.type, state.dispositionDetector
            )
            moreScanningRequired(state) -> {
                state.reached = DateTime.now()
                state
            }
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

        return when {
            elapsedTime(state.reached) > DEFAULT_DESIRED_DURATION -> {
                Log.d(TAG, "Complete the scan. Desired scan for ${state.type} found.")
                DocumentScanDisposition.Completed(state.type, state.dispositionDetector)
            }
            else -> state
        }
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
            DocumentOption.KENYA_DL_BACK -> {
                type == DocumentScanDisposition.DocumentScanType.KENYA_DL_BACK
            }
            DocumentOption.KENYA_DL_FRONT -> {
                type == DocumentScanDisposition.DocumentScanType.KENYA_DL_FRONT
            }
            DocumentOption.KENYA_ID_BACK -> {
                type == DocumentScanDisposition.DocumentScanType.KENYA_ID_BACK
            }
            DocumentOption.KENYA_ID_FRONT -> {
                type == DocumentScanDisposition.DocumentScanType.KENYA_DL_FRONT
            }
            DocumentOption.KENYA_PASSPORT -> {
                type == DocumentScanDisposition.DocumentScanType.PASSPORT
            }
            DocumentOption.INVALID -> false
        }
    }

    private fun moreScanningRequired(disposition: DocumentScanDisposition.Detected): Boolean {
        val seconds = elapsedTime(disposition.reached)
        return seconds < requiredTime
    }

    private fun elapsedTime(time: DateTime): Int {
        return Seconds.secondsBetween(currentTime, time).seconds
    }

    internal companion object {
        private val TAG = DocumentDispositionChanger::class.java.simpleName
        private const val THRESHOLD = 0.8f
        private const val DEFAULT_REQUIRED_SCAN_DURATION = 10 // time in seconds
        private const val DEFAULT_DESIRED_DURATION = 1 // time in seconds
    }
}