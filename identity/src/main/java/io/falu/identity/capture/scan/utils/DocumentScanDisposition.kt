package io.falu.identity.capture.scan.utils

import io.falu.identity.ai.DetectionOutput
import org.joda.time.DateTime

/**
 * Possible states when scanning a document
 */
internal sealed class DocumentScanDisposition(
    val type: DocumentScanType,
    val dispositionDetector: DocumentDispositionDetector,
    val terminate: Boolean
) {
    abstract fun next(output: DetectionOutput): DocumentScanDisposition

    /**
     *
     */
    internal enum class DocumentScanType {
        DL_BACK,
        DL_FRONT,
        ID_BACK,
        ID_FRONT,
        PASSPORT,
        SELFIE;

        val isFront: Boolean
            get() {
                return this == DL_FRONT ||
                        this == ID_FRONT ||
                        this == PASSPORT
            }

        val isBack: Boolean
            get() {
                return this == DL_BACK ||
                        this == ID_BACK
            }
    }

    /**
     *
     */
    internal class Start(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector, false) {
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
        DocumentScanDisposition(type, detector, false) {
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
        DocumentScanDisposition(type, detector, false) {
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
        DocumentScanDisposition(type, detector, false) {
        override fun next(output: DetectionOutput): DocumentScanDisposition =
            dispositionDetector.fromUndesired(this, output)
    }

    /**
     *
     */
    internal class Completed(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector, true) {
        override fun next(output: DetectionOutput): DocumentScanDisposition = this
    }

    /**
     *
     */
    internal class Timeout(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector, true) {
        override fun next(output: DetectionOutput) = this
    }
}