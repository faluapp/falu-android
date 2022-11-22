package io.falu.identity.capture.scan.utils

import io.falu.identity.ai.DetectionOutput

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
        IDENTITY_DOCUMENT_FRONT,
        IDENTITY_DOCUMENT_BACK,
        DL_FRONT,
        DL_BACK,
        PASSPORT
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
    internal class Detected(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput) = dispositionDetector.fromDetected(this, output)
    }

    /**
     *
     */
    internal class Desired(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput): DocumentScanDisposition = this
    }

    /**
     *
     */
    internal class Undesired(type: DocumentScanType, detector: DocumentDispositionDetector) :
        DocumentScanDisposition(type, detector) {
        override fun next(output: DetectionOutput): DocumentScanDisposition = this
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