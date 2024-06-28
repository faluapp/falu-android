package io.falu.identity.capture

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.core.exceptions.ApiException
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.ai.DocumentEngine
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraPermissionsFragment
import io.falu.identity.capture.scan.DocumentScanViewModel
import io.falu.identity.documents.DocumentSelectionFragment
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.utils.loadDocumentDetectionModel
import io.falu.identity.utils.matches
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.navigateToErrorFragment
import io.falu.identity.utils.rotate
import io.falu.identity.utils.serializable
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.toBitmap
import io.falu.identity.utils.updateVerification

internal abstract class AbstractCaptureFragment(
    identityViewModelFactory: ViewModelProvider.Factory
) : CameraPermissionsFragment() {

    protected val identityViewModel: IdentityVerificationViewModel by activityViewModels { identityViewModelFactory }

    private val documentScanViewModel: DocumentScanViewModel by activityViewModels { documentScanViewModelFactory }

    @VisibleForTesting
    internal var documentScanViewModelFactory: ViewModelProvider.Factory =
        DocumentScanViewModel.factoryProvider(this) { identityViewModel.modelPerformanceMonitor }

    @VisibleForTesting
    internal var captureDocumentViewModelFactory: ViewModelProvider.Factory =
        CaptureDocumentViewModel.CaptureDocumentViewModelFactory { this }

    protected val captureDocumentViewModel: CaptureDocumentViewModel by viewModels {
        captureDocumentViewModelFactory
    }

    protected lateinit var identityDocumentType: IdentityDocumentType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityDocumentType = requireArguments().serializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE)!!

        uploadStateObservations()
    }

    /**
     * Determine if the type of document provided manually or uploaded is valid
     *
     * @param uri
     */
    protected fun analyze(
        uri: Uri,
        scanType: ScanDisposition.DocumentScanType,
        documentSide: DocumentSide,
        type: UploadMethod = UploadMethod.MANUAL
    ) {
        val bitmap = uri.toBitmap(requireContext().contentResolver).rotate(90)

        loadDocumentDetectionModel(identityViewModel, documentScanViewModel, threshold) {
            val engine = DocumentEngine(it, threshold, identityViewModel.modelPerformanceMonitor)
            val output = engine.analyze(bitmap) as DocumentDetectionOutput

            if (output.score >= threshold && output.option.matches(scanType)) {
                reportSuccessfulAnalysisTelemetry(documentSide, output)
                uploadDocument(output.bitmap, documentSide, type)
            } else {
                findNavController().navigate(R.id.action_global_fragment_scan_capture_error)
            }
        }
    }

    private fun reportSuccessfulAnalysisTelemetry(documentSide: DocumentSide, output: DocumentDetectionOutput) {
        val telemetryDisposition = if (documentSide == DocumentSide.FRONT) {
            AnalyticsDisposition(frontModelScore = output.score)
        } else {
            AnalyticsDisposition(backModelScore = output.score)
        }

        identityViewModel.modifyAnalyticsDisposition(disposition = telemetryDisposition)
        documentScanViewModel.reportModelPerformance()
    }

    private fun uploadDocument(
        bitmap: Bitmap,
        documentSide: DocumentSide,
        type: UploadMethod = UploadMethod.MANUAL
    ) {
        if (documentSide == DocumentSide.FRONT) {
            showDocumentFrontUploading()
        } else {
            showDocumentBackUploading()
        }

        identityViewModel.uploadVerificationDocument(
            bitmap,
            documentSide,
            type = type,
            onError = {
                resetViews(documentSide)
                navigateToApiResponseProblemFragment((it as ApiException).problem)
            },
            onFailure = {
                resetViews(documentSide)
                navigateToErrorFragment(it)
            })
    }

    protected fun uploadScannedDocument(
        output: DocumentDetectionOutput,
        documentSide: DocumentSide
    ) {
        if (documentSide == DocumentSide.FRONT) {
            showDocumentFrontUploading()
        } else {
            showDocumentBackUploading()
        }

        identityViewModel.uploadScannedDocument(
            output.bitmap,
            documentSide,
            output.score,
            onError = {
                resetViews(documentSide)
                navigateToApiResponseProblemFragment((it as ApiException).problem)
            },
            onFailure = {
                resetViews(documentSide)
                navigateToErrorFragment(it)
            })
    }

    protected abstract fun showDocumentFrontUploading()

    protected abstract fun showDocumentBackUploading()

    protected abstract fun showDocumentFrontDoneUploading(disposition: DocumentUploadDisposition)

    protected abstract fun showDocumentBackDoneUploading()

    protected abstract fun showBothSidesUploaded(disposition: DocumentUploadDisposition)

    protected abstract fun resetViews(documentSide: DocumentSide)

    protected val isPassport: Boolean
        get() = identityDocumentType == IdentityDocumentType.PASSPORT

    private fun uploadStateObservations() {
        identityViewModel.documentUploadDisposition.observe(viewLifecycleOwner) {
            if (it.isFrontUpload) {
                showDocumentFrontDoneUploading(it)
            }

            if (it.isBackUploaded) {
                showDocumentBackDoneUploading()
            }

            if (identityDocumentType != IdentityDocumentType.PASSPORT) {
                if (it.isBothUploadLoad) {
                    showBothSidesUploaded(it)
                }
            }
        }
    }

    protected fun updateVerificationAndAttemptDocumentSubmission(
        @IdRes source: Int,
        verificationRequest: VerificationUploadRequest
    ) {
        val patchRequest = VerificationUpdateOptions(document = verificationRequest.document)

        updateVerification(identityViewModel, patchRequest, source, onSuccess = {
            attemptDocumentSubmission(source, verificationRequest)
        })
    }

    private fun attemptDocumentSubmission(
        @IdRes source: Int,
        verificationRequest: VerificationUploadRequest
    ) {
        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { verification ->
                when {
                    verification.selfieRequired -> {
                        findNavController().navigate(
                            R.id.action_global_fragment_selfie,
                            verificationRequest.addToBundle()
                        )
                    }

                    else -> {
                        submitVerificationData(identityViewModel, source, verificationRequest)
                    }
                }
            },
            onError = {
                navigateToApiResponseProblemFragment((it as ApiException).problem)
            }
        )
    }

    internal companion object {
        fun IdentityDocumentType.getIdentityDocumentName(context: Context) =
            context.getString(this.titleRes)

        private const val threshold = 0.75f
    }
}