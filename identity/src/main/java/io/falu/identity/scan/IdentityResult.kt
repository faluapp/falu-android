package io.falu.identity.scan

import io.falu.identity.ai.DetectionOutput

/**
 *
 */
internal data class ProvisionalResult(val disposition: ScanDisposition)

/**
 *
 */
internal data class IdentityResult(
    val output: DetectionOutput,
    val disposition: ScanDisposition
)

/**
 *
 */
internal data class ScanResult(
    var output: DetectionOutput? = null,
    var disposition: ScanDisposition? = null
) {
    fun modify(
        output: DetectionOutput? = null,
        disposition: ScanDisposition
    ) = this.copy(output = output, disposition = disposition)
}