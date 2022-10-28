package io.falu.identity.capture

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.VerificationDocumentSide
import io.falu.identity.api.models.verification.VerificationDocumentUpload
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraPermissionsFragment
import io.falu.identity.documents.DocumentSelectionFragment
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.navigateToErrorFragment
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.updateVerification
import software.tingle.api.patch.JsonPatchDocument

internal abstract class AbstractCaptureFragment : CameraPermissionsFragment() {
    protected val identityViewModel: IdentityVerificationViewModel by activityViewModels()

    private var captureDocumentViewModelFactory: ViewModelProvider.Factory =
        CaptureDocumentViewModel.CaptureDocumentViewModelFactory { this }

    protected val captureDocumentViewModel: CaptureDocumentViewModel by viewModels {
        captureDocumentViewModelFactory
    }

    protected var identityDocumentType: IdentityDocumentType? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityDocumentType =
            requireArguments().getSerializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE) as? IdentityDocumentType

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

    protected fun attemptDocumentSubmission(
        @IdRes source: Int,
        disposition: DocumentUploadDisposition
    ) {
        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { verification ->
                when {
                    verification.selfieRequired -> {
                        findNavController().navigate(R.id.action_global_fragment_selfie)
                    }
                    verification.videoRequired -> {

                    }
                    else -> {
                        val front = VerificationDocumentSide(
                            method = disposition.front!!.method!!,
                            file = disposition.front!!.file.id,
                        )
                        val back = if (identityDocumentType == IdentityDocumentType.PASSPORT) {
                            null
                        } else {
                            VerificationDocumentSide(
                                method = disposition.back!!.method!!,
                                file = disposition.back!!.file.id,
                            )
                        }

                        val document = VerificationDocumentUpload(
                            type = identityDocumentType!!,
                            front = front,
                            back = back
                        )

                        val uploadRequest = VerificationUploadRequest(document = document)
                        val patchDocument = JsonPatchDocument()
                            .replace("/document", document)

                        updateVerification(identityViewModel, patchDocument, source, onSuccess = {
                            submitVerificationData(identityViewModel, source, uploadRequest)
                        })
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