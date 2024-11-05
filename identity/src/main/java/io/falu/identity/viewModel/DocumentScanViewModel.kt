package io.falu.identity.viewModel

import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.savedstate.SavedStateRegistryOwner
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentDispositionMachine
import io.falu.identity.analytics.ModelPerformanceMonitor
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.scan.DocumentScanner
import io.falu.identity.scan.IdentityResult
import io.falu.identity.scan.ProvisionalResult
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.scan.ScanResult
import io.falu.identity.scan.ScanResultCallback
import io.falu.identity.utils.toFraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import kotlin.coroutines.CoroutineContext

internal class DocumentScanViewModel(private val performanceMonitor: ModelPerformanceMonitor) : ViewModel(),
    ScanResultCallback<ProvisionalResult, IdentityResult>, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO

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
    private lateinit var scanner: DocumentScanner

    internal fun initializeScanner(scanner: DocumentScanner) {
        this.scanner = scanner
        this.scanner.callbacks = this
    }

    internal fun startScan(
        owner: LifecycleOwner,
        capture: VerificationCapture,
        scanType: ScanDisposition.DocumentScanType
    ) {
        scanner.requireCameraView().bindLifecycle(owner)
        scanner.requireCameraView().startAnalyzer()

        scanner.disposition = null

        val machine = DocumentDispositionMachine(
            timeout = DateTime.now().plusMillis(capture.timeout),
            iou = capture.blur?.iou?.toFraction() ?: 0.95f,
            requiredTime = capture.blur?.duration?.div(1000) ?: 5
        )
        scanner.disposition = ScanDisposition.Start(scanType, machine)
    }

    internal fun stopScan(owner: LifecycleOwner) {
        scanner.requireCameraView().stopAnalyzer()
        scanner.requireCameraView().unbindFromLifecycle(owner)
    }

    override fun onScanComplete(result: IdentityResult) {
        Log.d(TAG, "Scan completed: $result")
        val documentDetectionOutput = result.output as DocumentDetectionOutput

        reportModelPerformance()

        _documentScanCompleteDisposition.update { current ->
            current.modify(output = documentDetectionOutput, disposition = result.disposition)
        }
    }

    override fun onProgress(result: ProvisionalResult) {
        Log.d(TAG, "Scan in progress: ${result.disposition}")

        scanner.changeDisposition(result.disposition) {
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

    internal fun reportModelPerformance() {
        launch(Dispatchers.IO) {
            performanceMonitor.reportModelPerformance(DOCUMENT_DETECTOR_MODEL)
        }
    }

    internal companion object {
        private const val DOCUMENT_DETECTOR_MODEL = "document_detector_v1"
        private val TAG = DocumentScanViewModel::class.java.simpleName
        fun factoryProvider(
            savedStateRegistryOwner: SavedStateRegistryOwner,
            performanceMonitor: () -> ModelPerformanceMonitor
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return DocumentScanViewModel(
                        performanceMonitor()
                    ) as T
                }
            }
    }
}