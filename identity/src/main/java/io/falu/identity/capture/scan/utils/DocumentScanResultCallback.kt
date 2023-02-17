package io.falu.identity.capture.scan.utils

import io.falu.identity.ai.DetectionOutput

internal interface DocumentScanResultCallback<ProvisionalResult, IdentityResult> {
    fun onScanComplete(result: IdentityResult)
    fun onProgress(result: ProvisionalResult)

    fun collectResults(output: DetectionOutput){

    }
}