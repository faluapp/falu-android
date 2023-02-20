package io.falu.identity.selfie

import android.util.Log
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.scan.ScanDispositionDetector
import io.falu.identity.scan.ScanDisposition
import org.joda.time.DateTime
import org.joda.time.Seconds

internal class FaceDispositionMachine(
    private val timeout: DateTime = DateTime.now().plusSeconds(8),
    private val currentTime: DateTime = DateTime.now(),
    private val requiredTime: Int = DEFAULT_REQUIRED_SCAN_DURATION,
) : ScanDispositionDetector {

    override fun fromStart(
        state: ScanDisposition.Start,
        output: DetectionOutput
    ): ScanDisposition {
        require(output is FaceDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            hasTimedOut -> {
                ScanDisposition.Timeout(state.type, this)
            }
            output.score >= threshold -> {
                Log.d(TAG, "Face detected, move to detected")
                ScanDisposition.Detected(state.type, this)
            }
            else -> {
                Log.d(TAG, "Face not detected, start disposition retained.")
                state
            }
        }
    }

    override fun fromDetected(
        state: ScanDisposition.Detected,
        output: DetectionOutput
    ): ScanDisposition {
        require(output is FaceDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            hasTimedOut -> {
                ScanDisposition.Timeout(state.type, this)
            }
            moreScanningRequired(state) -> {
                state.reached = DateTime.now()
                state
            }
            else -> {
                ScanDisposition.Desired(state.type, state.dispositionDetector)
            }
        }

    }

    override fun fromDesired(
        state: ScanDisposition.Desired,
        output: DetectionOutput
    ): ScanDisposition {
        return ScanDisposition.Completed(state.type, this)
    }

    override fun fromUndesired(
        state: ScanDisposition.Undesired,
        output: DetectionOutput
    ): ScanDisposition {
        return ScanDisposition.Start(state.type, this)
    }

    private fun moreScanningRequired(disposition: ScanDisposition.Detected): Boolean {
        val seconds = elapsedTime(time = disposition.reached)
        return seconds < requiredTime
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
        private const val DEFAULT_REQUIRED_SCAN_DURATION = 5 // time in seconds
    }
}