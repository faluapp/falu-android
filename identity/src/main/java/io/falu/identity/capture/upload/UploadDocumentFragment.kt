package io.falu.identity.capture.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.falu.identity.R
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}