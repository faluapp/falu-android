package io.falu.identity.scan

import io.falu.identity.ai.DetectionOutput

internal interface ScanDispositionDetector {
    fun fromStart(
        state: ScanDisposition.Start,
        output: DetectionOutput
    ): ScanDisposition

    fun fromDetected(
        state: ScanDisposition.Detected,
        output: DetectionOutput
    ): ScanDisposition

    fun fromDesired(
        state: ScanDisposition.Desired,
        output: DetectionOutput
    ): ScanDisposition

    fun fromUndesired(
        state: ScanDisposition.Undesired,
        output: DetectionOutput
    ): ScanDisposition
}