package io.falu.identity.ai

import android.util.Log
import io.falu.identity.scan.ScanDispositionDetector
import io.falu.identity.scan.ScanDisposition
import org.joda.time.DateTime
import org.joda.time.Seconds

internal class FaceDispositionMachine(
    private val timeout: DateTime = DateTime.now().plusSeconds(8),
    private val currentTime: DateTime = DateTime.now(),
    private val iou: Float = IOU_THRESHOLD,
    private val requiredTime: Int = DEFAULT_REQUIRED_SCAN_DURATION,
    private val desiredDuration: Int = DEFAULT_DESIRED_DURATION
) : ScanDispositionDetector {

    private var previousBoundingBox: BoundingBox? = null

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
            isRequiredScore(output) -> {
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
            !isRequiredScore(output) -> {
                Log.d(TAG, "Face not detected, score: ${output.score}; moving to undesired")
                ScanDisposition.Undesired(state.type, state.dispositionDetector)
            }
            !iouCheckSatisfied(output.box) -> {
                Log.d(TAG, "IOU check not satisfied")
                // reset the time
                state.reached = DateTime.now()
                state
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
        return when {
            elapsedTime(time = state.reached) > desiredDuration -> {
                Log.d(TAG, "Complete the scan. Desired scan for ${state.type} found.")
                ScanDisposition.Completed(state.type, state.dispositionDetector)
            }
            else -> state
        }
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

    private fun isRequiredScore(output: FaceDetectionOutput): Boolean = output.score >= threshold

    private fun elapsedTime(now: DateTime = currentTime, time: DateTime): Int {
        return Seconds.secondsBetween(now, time).seconds
    }

    private val hasTimedOut: Boolean
        get() {
            val elapsed = elapsedTime(now = timeout, time = DateTime.now())
            return elapsed > 0
        }

    private fun iouCheckSatisfied(currentBox: BoundingBox): Boolean {
        return previousBoundingBox?.let {
            val accuracy = calculateIOU(currentBox, it)
            previousBoundingBox = currentBox
            return accuracy >= iou
        } ?: run {
            previousBoundingBox = currentBox
            true
        }
    }

    internal companion object {
        private val TAG = FaceDispositionMachine::class.java.simpleName

        private const val threshold = 0.75
        private const val IOU_THRESHOLD = 0.95f
        private const val DEFAULT_REQUIRED_SCAN_DURATION = 5 // time in seconds
        private const val DEFAULT_DESIRED_DURATION = 0 // time in seconds
    }
}