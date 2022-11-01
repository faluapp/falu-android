package io.falu.identity.capture.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.R
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentManualCaptureBinding
import io.falu.identity.utils.FileUtils

internal class ManualCaptureFragment : AbstractCaptureFragment() {
    private var _binding: FragmentManualCaptureBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileUtils = FileUtils(requireContext())

        captureDocumentViewModel.captureDocumentImages(
            this,
            fileUtils,
            onFrontImageCaptured = {
                uploadDocument(uri = it, DocumentSide.FRONT, UploadMethod.MANUAL)
            },
            onBackImageCaptured = {
                uploadDocument(uri = it, DocumentSide.BACK, UploadMethod.MANUAL)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardDocumentBack.visibility =
            if (isPassport) View.GONE else View.VISIBLE
        binding.buttonContinue.isEnabled = false

        binding.tvUploadTitle.text =
            getString(
                R.string.upload_document_capture_title,
                identityDocumentType?.getIdentityDocumentName(requireContext())
            )
        binding.tvCardFront.text =
            getString(
                R.string.upload_document_capture_document_font,
                identityDocumentType?.getIdentityDocumentName(requireContext())
            )
        binding.tvCardBack.text =
            getString(
                R.string.upload_document_capture_document_back,
                identityDocumentType?.getIdentityDocumentName(requireContext())
            )

        binding.buttonSelectFront.setOnClickListener {
            captureDocumentViewModel.captureImageFront(requireContext())
        }

        binding.buttonSelectBack.setOnClickListener {
            captureDocumentViewModel.captureImageBack(requireContext())
        }

        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.setOnClickListener {
            binding.buttonContinue.showProgress()
            val disposition = binding.buttonContinue.tag as DocumentUploadDisposition

            updateVerificationAndAttemptDocumentSubmission(
                source = R.id.action_fragment_document_capture_methods_to_fragment_manual_capture,
                disposition.generateVerificationUploadRequest(identityDocumentType!!)
            )
        }
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