package io.falu.identity.capture.scan

import DocumentScanner
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.falu.identity.ai.DetectionOutput
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.capture.scan.utils.DocumentScanResultCallback
import io.falu.identity.capture.scan.utils.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

internal class DocumentScanViewModel :
    ViewModel(),
    DocumentScanResultCallback<ScanResult, DetectionOutput> {

    /**
     *
     */
    private val _documentScanDisposition = MutableStateFlow(ScanResult())
    val documentScanDisposition: LiveData<ScanResult>
        get() = _documentScanDisposition.asLiveData(Dispatchers.Main)

    /**
     *
     */
    internal var scanner: DocumentScanner? = null

    internal fun initialize(model: File, threshold: Float): DocumentScanner {
        if (scanner == null) {
            scanner = DocumentScanner(model, threshold, this)
        }
        return scanner!!
    }

    override fun onScanComplete(output: DetectionOutput) {
        Log.d(TAG, "Scan completed: $output")
    }

    override fun onProgress(result: ScanResult) {
        val documentDetectionOutput = result.output as DocumentDetectionOutput
        Log.d(
            TAG,
            "Scan in progress: ${result.disposition}, option: ${documentDetectionOutput.option}; score: ${documentDetectionOutput.score}"
        )

        _documentScanDisposition.update { current ->
            current.modify(output = documentDetectionOutput, disposition = result.disposition!!)
        }
    }

    internal companion object {
        private val TAG = DocumentScanViewModel::class.java.simpleName
    }
}