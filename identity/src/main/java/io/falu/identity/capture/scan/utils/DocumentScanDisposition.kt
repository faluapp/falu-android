package io.falu.identity.capture.scan.utils

import io.falu.identity.ai.DetectionOutput
import org.joda.time.DateTime

/**
 * Possible states when scanning a document
 */
internal sealed class DocumentScanDisposition(
    val type: DocumentScanType,
    val dispositionDetector: DocumentDispositionDetector
) {
    abstract fun next(output: DetectionOutput): DocumentScanDisposition

    /**
     *
     */
    internal enum class DocumentScanType {
        KENYA_DL_BACK,
        KENYA_DL_FRONT,
        KENYA_ID_BACK,
        KENYA_ID_FRONT,
        PASSPORT;

        val isFront: Boolean
            get() {
                return this == KENYA_DL_FRONT ||
                        this == KENYA_ID_FRONT ||
                        this == PASSPORT
            }

        val isBack: Boolean
            get() {
                return this == KENYA_DL_BACK ||
                        this == KENYA_ID_BACK
            }
    }

    /**
     *
     */
    internal class Start(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput): DocumentScanDisposition {
            return dispositionDetector.fromStart(this, output)
        }
    }

    /**
     *
     */
    internal class Detected(
        type: DocumentScanType,
        detector: DocumentDispositionDetector,
        internal var reached: DateTime = DateTime.now()
    ) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput) = dispositionDetector.fromDetected(this, output)
    }

    /**
     *
     */
    internal class Desired(
        type: DocumentScanType,
        detector: DocumentDispositionDetector,
        val reached: DateTime = DateTime.now()
    ) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput): DocumentScanDisposition =
            dispositionDetector.fromDesired(this, output)
    }

    /**
     *
     */
    internal class Undesired(
        type: DocumentScanType,
        detector: DocumentDispositionDetector,
        val reached: DateTime = DateTime.now()
    ) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput): DocumentScanDisposition =
            dispositionDetector.fromUndesired(this, output)
    }

    /**
     *
     */
    internal class Completed(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput): DocumentScanDisposition = this
    }

    /**
     *
     */
    internal class Timeout(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput) = this
    }
}