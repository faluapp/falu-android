package io.falu.identity.capture.scan.utils

import io.falu.identity.ai.DetectionOutput

internal data class ScanResult(
    var output: DetectionOutput? = null,
    var disposition: DocumentScanDisposition? = null
) {
    fun modify(
        output: DetectionOutput,
        disposition: DocumentScanDisposition
    ) = this.copy(output = output, disposition = disposition)
}