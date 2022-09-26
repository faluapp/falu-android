package io.falu.identity.capture.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.R
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
                uploadDocument(uri = it)
            },
            onBackImageCaptured = {
                uploadDocument(uri = it)
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

        binding.cardDocumentFront.setOnClickListener {
            captureDocumentViewModel.captureImageFront(requireContext())
        }

        binding.cardDocumentBack.setOnClickListener {
            captureDocumentViewModel.captureImageBack(requireContext())
        }
    }

    internal companion object {
        internal const val KEY_LAUNCH_CAMERA_DOCUMENT_FRONT = ":document-front"
        internal const val KEY_LAUNCH_CAMERA_DOCUMENT_BACK = ":document-back"
    }
}