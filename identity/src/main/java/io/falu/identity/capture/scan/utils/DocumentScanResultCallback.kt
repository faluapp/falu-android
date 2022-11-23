package io.falu.identity.capture.scan.utils

internal interface DocumentScanResultCallback<Result> {
    fun onScanComplete(result: Result)
    fun onProgress(result: Result)
}