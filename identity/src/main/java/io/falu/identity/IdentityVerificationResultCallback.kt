package io.falu.identity

internal interface IdentityVerificationResultCallback {
    fun onFinishWithResult(result: IdentityVerificationResult)
}