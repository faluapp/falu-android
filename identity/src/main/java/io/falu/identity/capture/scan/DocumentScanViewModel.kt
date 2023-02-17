package io.falu.identity.capture.scan

import DocumentScanner
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.capture.scan.utils.DocumentScanResultCallback
import io.falu.identity.capture.scan.utils.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

internal class DocumentScanViewModel :
    ViewModel(),
    DocumentScanResultCallback<DocumentScanner.ProvisionalResult, DocumentScanner.IdentityResult> {

    private var previousDisposition: DocumentScanDisposition? = null
    private var currentDisposition: DocumentScanDisposition? = null

    /**
     *
     */
    private val _documentScanDisposition = MutableLiveData<DocumentScanner.ProvisionalResult?>()
    val documentScanDisposition: LiveData<DocumentScanner.ProvisionalResult?>
        get() = _documentScanDisposition

    /**
     *
     */
    private val _documentScanCompleteDisposition = MutableStateFlow(ScanResult())
    val documentScanCompleteDisposition: LiveData<ScanResult>
        get() = _documentScanCompleteDisposition.asLiveData(Dispatchers.Main)

    /**
     *
     */
    internal var scanner: DocumentScanner? = null

    internal fun initialize(model: File, threshold: Float) {
        scanner = DocumentScanner(model, threshold, this)
    }

    override fun onScanComplete(result: DocumentScanner.IdentityResult) {
        Log.d(TAG, "Scan completed: $result")
        val documentDetectionOutput = result.output as DocumentDetectionOutput

        _documentScanCompleteDisposition.update { current ->
            current.modify(output = documentDetectionOutput, disposition = result.disposition)
        }
    }

    override fun onProgress(result: DocumentScanner.ProvisionalResult) {
        Log.d(TAG, "Scan in progress: ${result.disposition}")

        changeDisposition(result.disposition) {
            if (it) {
                _documentScanDisposition.postValue(result)
            }
        }
    }

    internal fun resetScanDispositions() {
        _documentScanDisposition.postValue(null)
        _documentScanCompleteDisposition.update { ScanResult() }
    }

    private fun changeDisposition(
        disposition: DocumentScanDisposition,
        change: ((Boolean) -> Unit)
    ) {
        if (disposition == previousDisposition && previousDisposition?.terminate == true) {
            change(false)
            return
        }

        currentDisposition = disposition
        previousDisposition = disposition
        change(true)
    }

    internal companion object {
        private val TAG = DocumentScanViewModel::class.java.simpleName
    }
}