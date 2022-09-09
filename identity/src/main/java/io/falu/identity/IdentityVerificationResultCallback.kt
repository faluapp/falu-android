package io.falu.identity

/**
 * Inform when the verification process completes and the a result is available
 */
interface IdentityVerificationResultCallback {
    fun onVerificationResult(result: IdentityVerificationResult?)
}