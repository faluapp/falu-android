package io.falu.identity.capture.scan

import android.util.Log
import io.falu.identity.ai.BoundingBox
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentOption
import io.falu.identity.ai.calculateIOU
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.scan.ScanDispositionDetector
import org.joda.time.DateTime
import org.joda.time.Seconds

internal class DocumentDispositionMachine(
    private val timeout: DateTime = DateTime.now().plusSeconds(8),
    private val iou: Float = IOU_THRESHOLD,
    private val requiredTime: Int = DEFAULT_REQUIRED_SCAN_DURATION,
    private val requireThreshold: Float = DEFUALT_REQUIRED_THRESHOLD,
    private val currentTime: DateTime = DateTime.now(),
    private val undesiredDuration: Int = DEFAULT_UNDESIRED_DURATION,
    private val desiredDuration: Int = DEFAULT_DESIRED_DURATION
) : ScanDispositionDetector {

    private var previousBoundingBox: BoundingBox? = null
    private var matcherCounter = 0

    override fun fromStart(
        state: ScanDisposition.Start,
        output: DetectionOutput
    ): ScanDisposition {
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            hasTimedOut -> {
                ScanDisposition.Timeout(state.type, this)
            }

            output.option.matches(state.type) -> {
                Log.d(
                    TAG, "Model output detected with score ${output.score}, " +
                            "moving to Detected."
                )
                ScanDisposition.Detected(state.type, this)
            }

            else -> {
                Log.d(
                    TAG, "Model output mismatch (${output.option})," +
                            "start disposition retained."
                )
                state
            }
        }
    }

    override fun fromDetected(
        state: ScanDisposition.Detected,
        output: DetectionOutput
    ): ScanDisposition {
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }

        return when {
            hasTimedOut -> {
                ScanDisposition.Timeout(state.type, this)
            }

            !targetTypeMatches(output.option, state.type) -> {
                Log.d(TAG, "Option (${output.option}) doesn't match ${state.type}")
                ScanDisposition.Undesired(state.type, state.dispositionDetector)
            }

            output.score < requireThreshold -> {
                Log.d(
                    TAG,
                    "Score (${output.score}) for (${output.option}) " +
                            "doesn't meet the required threshold."
                )
                state.reached = DateTime.now()
                state
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
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }

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
        return when {
            hasTimedOut -> {
                ScanDisposition.Timeout(state.type, this)
            }

            elapsedTime(time = state.reached) > undesiredDuration -> {
                Log.d(TAG, "Scan for ${state.type} undesired, restarting the process.")
                ScanDisposition.Start(state.type, state.dispositionDetector)
            }

            else -> state
        }
    }

    private fun DocumentOption.matches(
        type: ScanDisposition.DocumentScanType
    ): Boolean {
        return this == DocumentOption.DL_BACK && type == ScanDisposition.DocumentScanType.DL_BACK ||
                this == DocumentOption.DL_FRONT && type == ScanDisposition.DocumentScanType.DL_FRONT ||
                this == DocumentOption.ID_BACK && type == ScanDisposition.DocumentScanType.ID_BACK ||
                this == DocumentOption.ID_FRONT && type == ScanDisposition.DocumentScanType.ID_FRONT ||
                this == DocumentOption.PASSPORT && type == ScanDisposition.DocumentScanType.PASSPORT
    }

    private fun targetTypeMatches(
        option: DocumentOption,
        type: ScanDisposition.DocumentScanType
    ): Boolean {
        return if (option.matches(type)) {
            // reset counter
            matcherCounter = 0
            true
        } else {
            matcherCounter++
            matcherCounter <= DEFAULT_MATCH_COUNTER
        }
    }

    private fun moreScanningRequired(disposition: ScanDisposition.Detected): Boolean {
        val seconds = elapsedTime(time = disposition.reached)
        return seconds < requiredTime
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

    private fun elapsedTime(now: DateTime = currentTime, time: DateTime): Int {
        return Seconds.secondsBetween(now, time).seconds
    }

    private val hasTimedOut: Boolean
        get() {
            val elapsed = elapsedTime(now = timeout, time = DateTime.now())
            return elapsed > 0
        }

    internal companion object {
        private val TAG = DocumentDispositionMachine::class.java.simpleName
        private const val IOU_THRESHOLD = 0.95f
        private const val DEFUALT_REQUIRED_THRESHOLD = 0.75f
        private const val DEFAULT_MATCH_COUNTER = 1
        private const val DEFAULT_REQUIRED_SCAN_DURATION = 5 // time in seconds
        private const val DEFAULT_DESIRED_DURATION = 3 // time in seconds
        private const val DEFAULT_UNDESIRED_DURATION = 0 // time in seconds
    }
}