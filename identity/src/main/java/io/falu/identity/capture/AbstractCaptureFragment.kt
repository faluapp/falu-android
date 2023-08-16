package io.falu.identity.capture

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraPermissionsFragment
import io.falu.identity.documents.DocumentSelectionFragment
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.navigateToErrorFragment
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.updateVerification
import software.tingle.api.patch.JsonPatchDocument

internal abstract class AbstractCaptureFragment(
    identityViewModelFactory: ViewModelProvider.Factory
) : CameraPermissionsFragment() {

    protected val identityViewModel: IdentityVerificationViewModel
            by activityViewModels { identityViewModelFactory }

    @VisibleForTesting
    internal var captureDocumentViewModelFactory: ViewModelProvider.Factory =
        CaptureDocumentViewModel.CaptureDocumentViewModelFactory { this }

    protected val captureDocumentViewModel: CaptureDocumentViewModel by viewModels {
        captureDocumentViewModelFactory
    }

    protected lateinit var identityDocumentType: IdentityDocumentType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityDocumentType =
            requireArguments().getSerializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE)
                    as IdentityDocumentType

        uploadStateObservations()
    }

    protected fun uploadDocument(
        uri: Uri,
        documentSide: DocumentSide,
        type: UploadMethod = UploadMethod.MANUAL
    ) {
        if (documentSide == DocumentSide.FRONT) {
            showDocumentFrontUploading()
        } else {
            showDocumentBackUploading()
        }

        identityViewModel.uploadVerificationDocument(
            uri,
            documentSide,
            type = type,
            onError = {
                resetViews(documentSide)
                navigateToApiResponseProblemFragment(it)
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
                navigateToApiResponseProblemFragment(it)
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
        val patchDocument = JsonPatchDocument()
            .replace("/document", verificationRequest.document)

        updateVerification(identityViewModel, patchDocument, source, onSuccess = {
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
                navigateToApiResponseProblemFragment(it)
            }
        )
    }

    internal companion object {
        fun IdentityDocumentType.getIdentityDocumentName(context: Context) =
            context.getString(this.titleRes)
    }
}