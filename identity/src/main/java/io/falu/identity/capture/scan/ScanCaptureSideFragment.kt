package io.falu.identity.capture.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import io.falu.identity.R
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentCaptureSideBinding

internal class ScanCaptureSideFragment(identityViewModelFactory: ViewModelProvider.Factory) :
    AbstractCaptureFragment(identityViewModelFactory) {

    private var _binding: FragmentCaptureSideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureSideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardDocumentBack.visibility =
            if (isPassport) View.GONE else View.VISIBLE
        binding.buttonContinue.isEnabled = false

        binding.tvScanTitle.text =
            getString(
                R.string.scan_capture_title_scan,
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

        binding.buttonContinue.text = getString(R.string.button_continue)

        binding.buttonScanFront.setOnClickListener {  }

        binding.buttonScanBack.setOnClickListener {  }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showDocumentFrontUploading() {
    }

    override fun showDocumentBackUploading() {
    }

    override fun showDocumentFrontDoneUploading(disposition: DocumentUploadDisposition) {
    }

    override fun showDocumentBackDoneUploading() {
    }

    override fun showBothSidesUploaded(disposition: DocumentUploadDisposition) {
    }

    override fun resetViews(documentSide: DocumentSide) {
    }
}