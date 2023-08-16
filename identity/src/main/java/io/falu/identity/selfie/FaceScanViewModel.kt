package io.falu.identity.selfie

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.scan.ScanResultCallback
import io.falu.identity.scan.ScanResult
import io.falu.identity.scan.IdentityResult
import io.falu.identity.scan.ProvisionalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

internal class FaceScanViewModel : ViewModel(),
    ScanResultCallback<ProvisionalResult, IdentityResult> {

    /**
     *
     */
    private val _faceScanDisposition = MutableStateFlow(ScanResult())
    val faceScanDisposition: LiveData<ScanResult>
        get() = _faceScanDisposition.asLiveData(Dispatchers.Main)

    /**
     *
     */
    private val _faceScanCompleteDisposition = MutableStateFlow(ScanResult())
    val faceScanCompleteDisposition: LiveData<ScanResult>
        get() = _faceScanCompleteDisposition.asLiveData(Dispatchers.Main)

    /***/
    internal var scanner: FaceScanner? = null

    internal fun initialize(model: File, threshold: Float) {
        scanner = FaceScanner(model, threshold, this)
    }

    override fun onScanComplete(result: IdentityResult) {
        Log.d(TAG, "Scan completed: $result")
        val faceDetectionOutput = result.output as FaceDetectionOutput

        _faceScanCompleteDisposition.update { current ->
            current.modify(output = faceDetectionOutput, disposition = result.disposition)
        }
    }

    override fun onProgress(result: ProvisionalResult) {
        Log.d(TAG, "Scan in progress: ${result.disposition}")

        _faceScanDisposition.update { current ->
            current.modify(disposition = result.disposition)
        }
    }

    internal fun resetScanDispositions() {
        _faceScanCompleteDisposition.update { ScanResult() }
        _faceScanDisposition.update { ScanResult() }
    }

    companion object {
        private val TAG = FaceScanViewModel::class.java.simpleName
    }
}