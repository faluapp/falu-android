package io.falu.identity.capture.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModelProvider
import io.falu.identity.R
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.camera.CameraView
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.databinding.FragmentScanCaptureBinding

internal class ScanCaptureFragment(identityViewModelFactory: ViewModelProvider.Factory) :
    AbstractCaptureFragment(identityViewModelFactory) {
    private var _binding: FragmentScanCaptureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvScanDocumentSide.text = getString(
            R.string.scan_capture_text_document_side,
            identityDocumentType?.getIdentityDocumentName(requireContext())
        )

        binding.tvScanMessage.text = getString(
            R.string.scan_capture_text_scan_message,
            identityDocumentType?.getIdentityDocumentName(requireContext())
        )

        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_BACK
        binding.viewCamera.cameraViewType =
            if (identityDocumentType != IdentityDocumentType.PASSPORT) CameraView.CameraViewType.ID else CameraView.CameraViewType.PASSPORT

        binding.buttonContinue.text = getString(R.string.button_continue)

        binding.buttonContinue.isEnabled = false
        binding.buttonContinue.setOnClickListener {

        }
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