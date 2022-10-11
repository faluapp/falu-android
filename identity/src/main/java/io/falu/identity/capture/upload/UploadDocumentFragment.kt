package io.falu.identity.capture.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.R
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentUploadDocumentBinding

internal class UploadDocumentFragment : AbstractCaptureFragment() {
    private var _binding: FragmentUploadDocumentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        captureDocumentViewModel.pickDocumentImages(
            fragment = this,
            onFrontImagePicked = {
                uploadDocument(uri = it, DocumentSide.FRONT)
            },
            onBackImagePicked = {
                uploadDocument(uri = it, DocumentSide.BACK)
            }
        )

        identityViewModel.observerForDocumentUploadResults(
            viewLifecycleOwner,
            onSuccess = {
                println("AKAKAKAKAKAKAAK")
            },
            onFailure = {
                resetFrontUploading()
            }
        )

        binding.buttonSelectFront.setOnClickListener {
            captureDocumentViewModel.pickImageFront()
        }

        binding.buttonSelectBack.setOnClickListener {
            captureDocumentViewModel.pickImageBack()
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

    override fun showDocumentFrontDoneUploading() {
        binding.buttonSelectFront.visibility = View.GONE
        binding.progressSelectFront.visibility = View.GONE
        binding.ivFrontUploaded.visibility = View.VISIBLE
    }

    override fun showDocumentBackDoneUploading() {
        binding.buttonSelectBack.visibility = View.GONE
        binding.progressSelectBack.visibility = View.GONE
        binding.ivBackUploaded.visibility = View.VISIBLE
    }

    private fun resetFrontUploading(){
        binding.buttonSelectFront.visibility = View.VISIBLE
        binding.progressSelectFront.visibility = View.GONE
        binding.ivFrontUploaded.visibility = View.GONE
    }
}