package io.falu.identity.capture.scan.utils

import android.util.Log
import io.falu.identity.ai.BoundingBox
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentOption
import org.joda.time.DateTime
import org.joda.time.Seconds
import kotlin.math.max
import kotlin.math.min


internal class DocumentDispositionMachine(
    private val timeout: DateTime = DateTime.now().plusSeconds(8),
    private val iou: Float = IOU_THRESHOLD,
    private val requiredTime: Int = DEFAULT_REQUIRED_SCAN_DURATION,
    private val currentTime: DateTime = DateTime.now(),
    private val undesiredDuration: Int = DEFAULT_UNDESIRED_DURATION,
    private val desiredDuration: Int = DEFAULT_DESIRED_DURATION
) : DocumentDispositionDetector {

    private var previousBoundingBox: BoundingBox? = null
    private var matcherCounter = 0

    override fun fromStart(
        state: DocumentScanDisposition.Start,
        output: DetectionOutput
    ): DocumentScanDisposition {
        require(output is DocumentDetectionOutput) {
            "Unexpected output type: $output"
        }
        return when {
            hasTimedOut -> {
                DocumentScanDisposition.Timeout(state.type, this)
            }
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
            hasTimedOut -> {
                DocumentScanDisposition.Timeout(state.type, this)
            }
            !targetTypeMatches(output.option, state.type) -> {
                Log.d(TAG, "Option (${output.option}) doesn't match ${state.type}")
                DocumentScanDisposition.Undesired(state.type, state.dispositionDetector)
            }
            !iouCheckSatisfied(output.box) -> {
                // reset the time
                state.reached = DateTime.now()
                state
            }
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
            elapsedTime(time = state.reached) > desiredDuration -> {
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
        return when {
            hasTimedOut -> {
                DocumentScanDisposition.Timeout(state.type, this)
            }
            elapsedTime(time = state.reached) > undesiredDuration -> {
                Log.d(TAG, "Scan for ${state.type} undesired, restarting the process.")
                DocumentScanDisposition.Start(state.type, state.dispositionDetector)
            }
            else -> state
        }
    }

    private fun DocumentOption.matches(
        type: DocumentScanDisposition.DocumentScanType
    ): Boolean {
        return this == DocumentOption.DL_BACK && type == DocumentScanDisposition.DocumentScanType.DL_BACK ||
                this == DocumentOption.DL_FRONT && type == DocumentScanDisposition.DocumentScanType.DL_FRONT ||
                this == DocumentOption.ID_BACK && type == DocumentScanDisposition.DocumentScanType.ID_BACK ||
                this == DocumentOption.ID_FRONT && type == DocumentScanDisposition.DocumentScanType.ID_FRONT ||
                this == DocumentOption.PASSPORT && type == DocumentScanDisposition.DocumentScanType.PASSPORT
    }

    private fun targetTypeMatches(
        option: DocumentOption,
        type: DocumentScanDisposition.DocumentScanType
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

    private fun moreScanningRequired(disposition: DocumentScanDisposition.Detected): Boolean {
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

    /**
     * Measure the accuracy of detection
     */
    private fun calculateIOU(currentBox: BoundingBox, previousBox: BoundingBox): Float {
        val currentLeft = currentBox.left
        val currentRight = currentBox.left + currentBox.width
        val currentTop = currentBox.top
        val currentBottom = currentBox.top + currentBox.height

        val previousLeft = previousBox.left
        val previousRight = previousBox.left + previousBox.width
        val previousTop = previousBox.top
        val previousBottom = previousBox.top + previousBox.height

        // determine the (x, y)-coordinates of the intersection rectangle
        val xA = max(currentLeft, previousLeft)
        val yA = max(currentTop, previousTop)
        val xB = min(currentRight, previousRight)
        val yB = min(currentBottom, previousBottom)

        // compute the area of intersection rectangle

        // compute the area of intersection rectangle
        val intersectionArea = (xB - xA) * (yB - yA)

        // compute the area of both the prediction and ground-truth
        // rectangles
        val currentBoxArea = (currentRight - currentLeft) * (currentBottom - currentTop)
        val previousBoxArea = (previousRight - previousLeft) * (previousBottom - previousTop)

        // compute the intersection over union by taking the intersection
        // area and dividing it by the sum of prediction + ground-truth
        // areas - the intersection area
        val iou = intersectionArea / (currentBoxArea + previousBoxArea - intersectionArea)

        Log.d(TAG, "Calculated box accuracy: $iou")
        return iou
    }

    internal companion object {
        private val TAG = DocumentDispositionMachine::class.java.simpleName
        private const val IOU_THRESHOLD = 0.95f
        private const val DEFAULT_MATCH_COUNTER = 1
        private const val DEFAULT_REQUIRED_SCAN_DURATION = 5 // time in seconds
        private const val DEFAULT_DESIRED_DURATION = 3 // time in seconds
        private const val DEFAULT_UNDESIRED_DURATION = 0 // time in seconds
    }
}