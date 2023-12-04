package io.falu.identity.selfie

import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.scan.IdentityResult
import io.falu.identity.scan.ProvisionalResult
import io.falu.identity.scan.ScanResult
import io.falu.identity.scan.ScanResultCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class FaceScanViewModel(private val performanceMonitor: ModelPerformanceMonitor) : ViewModel(),
    ScanResultCallback<ProvisionalResult, IdentityResult>, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO

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
        scanner = FaceScanner(model, threshold, performanceMonitor, this)
    }

    override fun onScanComplete(result: IdentityResult) {
        Log.d(TAG, "Scan completed: $result")
        val faceDetectionOutput = result.output as FaceDetectionOutput

        reportModelPerformance()

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

    internal fun reportModelPerformance() {
        launch(Dispatchers.IO) {
            performanceMonitor.reportModelPerformance(FACE_DETECTOR_MODEL)
        }
    }

    companion object {
        private const val FACE_DETECTOR_MODEL = "face_detector_v1"
        private val TAG = FaceScanViewModel::class.java.simpleName

        fun factoryProvider(
            savedStateRegistryOwner: SavedStateRegistryOwner,
            performanceMonitor: () -> ModelPerformanceMonitor,

            ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return FaceScanViewModel(
                        performanceMonitor(),
                    ) as T
                }
            }
    }
}