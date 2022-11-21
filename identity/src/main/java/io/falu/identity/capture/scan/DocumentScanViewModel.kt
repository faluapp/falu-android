package io.falu.identity.capture.scan

import DocumentScanner
import androidx.lifecycle.ViewModel
import io.falu.identity.ai.DetectionOutput
import java.io.File

internal class DocumentScanViewModel : ViewModel(),
    DocumentScanResultCallback<String, DetectionOutput> {

    private var scanner: DocumentScanner? = null

    internal fun initialize(model: File, threshold: Float): DocumentScanner {
        if (scanner == null) {
            scanner = DocumentScanner(model, threshold, this)
        }
        return scanner!!
    }

    override fun onScanComplete(result: DetectionOutput) {
        TODO("Not yet implemented")
    }

    override fun onProgress(result: String) {
        TODO("Not yet implemented")
    }
}

interface DocumentScanResultCallback<State, Output> {
    fun onScanComplete(result: Output)
    fun onProgress(result: State)
}