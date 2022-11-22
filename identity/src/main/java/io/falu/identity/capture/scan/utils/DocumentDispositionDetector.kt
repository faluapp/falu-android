package io.falu.identity.capture.scan.utils

import io.falu.identity.ai.DetectionOutput

internal interface DocumentDispositionDetector {
    fun fromStart(
        state: DocumentScanDisposition.Start,
        output: DetectionOutput
    ): DocumentScanDisposition

    fun fromDetected(
        state: DocumentScanDisposition.Detected,
        output: DetectionOutput
    ): DocumentScanDisposition

    fun fromDesired(
        state: DocumentScanDisposition.Desired,
        output: DetectionOutput
    ): DocumentScanDisposition

    fun fromUndesired(
        state: DocumentScanDisposition.Undesired,
        output: DetectionOutput
    ): DocumentScanDisposition
}