package io.falu.identity

/**
 * A Callback interface for Falu Identity SDK
 */
interface IdentityVerificationCallback {
    fun onVerificationResult(result: IdentityVerificationResult)
}