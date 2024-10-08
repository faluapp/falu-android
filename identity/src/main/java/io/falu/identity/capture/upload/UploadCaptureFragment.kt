package io.falu.identity.capture.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_UPLOAD_CAPTURE
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.capture.scan.ScanCaptureFragment.Companion.getScanType
import io.falu.identity.databinding.FragmentUploadCaptureBinding

internal class UploadCaptureFragment(identityViewModelFactory: ViewModelProvider.Factory) :
    AbstractCaptureFragment(identityViewModelFactory) {
    private var _binding: FragmentUploadCaptureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        identityViewModel.reportTelemetry(
            identityViewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_UPLOAD_CAPTURE)
        )

        binding.cardDocumentBack.visibility =
            if (isPassport) View.GONE else View.VISIBLE
        binding.buttonContinue.isEnabled = false

        binding.tvUploadTitle.text =
            getString(
                R.string.upload_document_capture_title,
                identityDocumentType.getIdentityDocumentName(requireContext())
            )
        binding.tvCardFront.text =
            getString(
                R.string.upload_document_capture_document_font,
                identityDocumentType.getIdentityDocumentName(requireContext())
            )
        binding.tvCardBack.text =
            getString(
                R.string.upload_document_capture_document_back,
                identityDocumentType.getIdentityDocumentName(requireContext())
            )

        captureDocumentViewModel.pickDocumentImages(
            fragment = this,
            onFrontImagePicked = {
                analyze(uri = it, identityDocumentType.getScanType().first, DocumentSide.FRONT, UploadMethod.UPLOAD)
            },
            onBackImagePicked = {
                identityDocumentType.getScanType().second?.let { scanType ->
                    analyze(
                        uri = it,
                        scanType,
                        DocumentSide.BACK,
                        UploadMethod.UPLOAD
                    )
                }
            }
        )

        binding.buttonSelectFront.setOnClickListener {
            captureDocumentViewModel.pickImageFront()
        }

        binding.buttonSelectBack.setOnClickListener {
            captureDocumentViewModel.pickImageBack()
        }

        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.setOnClickListener {
            binding.buttonContinue.showProgress()
            val disposition = binding.buttonContinue.tag as DocumentUploadDisposition
            updateVerificationAndAttemptDocumentSubmission(
                source = R.id.action_fragment_document_capture_methods_to_fragment_upload_capture,
                disposition.generateVerificationUploadRequest(identityDocumentType)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showDocumentFrontUploading() {
        binding.buttonSelectFront.visibility = View.GONE
        binding.progressSelectFront.visibility = View.VISIBLE
        binding.ivFrontUploaded.visibility = View.GONE
    }

    override fun showDocumentBackUploading() {
        binding.buttonSelectBack.visibility = View.GONE
        binding.progressSelectBack.visibility = View.VISIBLE
        binding.ivBackUploaded.visibility = View.GONE
    }

    override fun showDocumentFrontDoneUploading(disposition: DocumentUploadDisposition) {
        binding.buttonSelectFront.visibility = View.GONE
        binding.progressSelectFront.visibility = View.GONE
        binding.ivFrontUploaded.visibility = View.VISIBLE

        if (identityDocumentType == IdentityDocumentType.PASSPORT) {
            binding.buttonContinue.isEnabled = true
            binding.buttonContinue.tag = disposition
        }
    }

    override fun showDocumentBackDoneUploading() {
        binding.buttonSelectBack.visibility = View.GONE
        binding.progressSelectBack.visibility = View.GONE
        binding.ivBackUploaded.visibility = View.VISIBLE
    }

    override fun resetViews(documentSide: DocumentSide) {
        if (documentSide == DocumentSide.FRONT) {
            binding.buttonSelectFront.visibility = View.VISIBLE
            binding.progressSelectFront.visibility = View.GONE
            binding.ivFrontUploaded.visibility = View.GONE
        } else {
            binding.buttonSelectBack.visibility = View.VISIBLE
            binding.progressSelectBack.visibility = View.GONE
            binding.ivBackUploaded.visibility = View.GONE
        }
    }

    override fun showBothSidesUploaded(disposition: DocumentUploadDisposition) {
        binding.buttonContinue.isEnabled = true
        binding.buttonContinue.tag = disposition
    }
}