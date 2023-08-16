package io.falu.identity.capture.scan

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.scan.ScanResultCallback
import io.falu.identity.scan.ScanResult
import io.falu.identity.scan.IdentityResult
import io.falu.identity.scan.ProvisionalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

internal class DocumentScanViewModel : ViewModel(),
    ScanResultCallback<ProvisionalResult, IdentityResult> {

    /**
     *
     */
    private val _documentScanDisposition = MutableStateFlow(ScanResult())
    val documentScanDisposition: LiveData<ScanResult>
        get() = _documentScanDisposition.asLiveData(Dispatchers.Main)

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

    override fun onScanComplete(result: IdentityResult) {
        Log.d(TAG, "Scan completed: $result")
        val documentDetectionOutput = result.output as DocumentDetectionOutput

        _documentScanCompleteDisposition.update { current ->
            current.modify(output = documentDetectionOutput, disposition = result.disposition)
        }
    }

    override fun onProgress(result: ProvisionalResult) {
        Log.d(TAG, "Scan in progress: ${result.disposition}")

        scanner?.changeDisposition(result.disposition) {
            if (it) {
                _documentScanDisposition.update { current ->
                    current.modify(disposition = result.disposition)
                }
            }
        }
    }

    internal fun resetScanDispositions() {
        _documentScanDisposition.update { ScanResult() }
        _documentScanCompleteDisposition.update { ScanResult() }
    }

    internal companion object {
        private val TAG = DocumentScanViewModel::class.java.simpleName
    }
}