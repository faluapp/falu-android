package io.falu.identity.capture.scan.utils

internal interface DocumentScanResultCallback<Result, Output> {
    fun onScanComplete(output: Output)
    fun onProgress(result: Result)
}