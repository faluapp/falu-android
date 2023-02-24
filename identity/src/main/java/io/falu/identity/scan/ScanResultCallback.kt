package io.falu.identity.scan

internal interface ScanResultCallback<ProvisionalResult, IdentityResult> {
    fun onScanComplete(result: IdentityResult)
    fun onProgress(result: ProvisionalResult)
}