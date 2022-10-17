package io.falu.identity.capture.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.R
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentPhotoUploadBinding
import io.falu.identity.utils.FileUtils

internal class PhotoUploadFragment : AbstractCaptureFragment() {
    private var _binding: FragmentPhotoUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileUtils = FileUtils(requireContext())

        captureDocumentViewModel.captureDocumentImages(
            fragment = this,
            fileUtils = fileUtils,
            onFrontImageCaptured = {
                uploadDocument(uri = it, DocumentSide.FRONT)
            },
            onBackImageCaptured = {
                uploadDocument(uri = it, DocumentSide.BACK)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardDocumentBack.visibility =
            if (isPassport) View.GONE else View.GONE

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
}