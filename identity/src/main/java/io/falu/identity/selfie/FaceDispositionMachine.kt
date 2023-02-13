package io.falu.identity.selfie

import android.util.Log
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.capture.scan.utils.DocumentDispositionDetector
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import org.joda.time.DateTime
import org.joda.time.Seconds

internal class FaceDispositionMachine(
    private val timeout: DateTime = DateTime.now().plusSeconds(8),
    private val currentTime: DateTime = DateTime.now(),
) : DocumentDispositionDetector {

    override fun fromStart(
        state: DocumentScanDisposition.Start,
        output: DetectionOutput
    ): DocumentScanDisposition {
        require(output is FaceDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            hasTimedOut -> {
                DocumentScanDisposition.Timeout(state.type, this)
            }
            output.score >= threshold -> {
                Log.d(TAG, "Face detected, move to detected")
                DocumentScanDisposition.Detected(state.type, this)
            }
            else -> {
                Log.d(TAG, "Face not detected, start disposition retained.")
                state
            }
        }
    }

    override fun fromDetected(
        state: DocumentScanDisposition.Detected,
        output: DetectionOutput
    ): DocumentScanDisposition {
        require(output is FaceDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            hasTimedOut -> {
                DocumentScanDisposition.Timeout(state.type, this)
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
        return DocumentScanDisposition.Completed(state.type, this)
    }

    override fun fromUndesired(
        state: DocumentScanDisposition.Undesired,
        output: DetectionOutput
    ): DocumentScanDisposition {
        return DocumentScanDisposition.Start(state.type, this)
    }

    private fun elapsedTime(now: DateTime = currentTime, time: DateTime): Int {
        return Seconds.secondsBetween(now, time).seconds
    }

    private val hasTimedOut: Boolean
        get() {
            val elapsed = elapsedTime(now = timeout, time = DateTime.now())
            return elapsed > 0
        }

    internal companion object {
        private val TAG = FaceDispositionMachine::class.java.simpleName

        private const val threshold = 0.75
    }
}