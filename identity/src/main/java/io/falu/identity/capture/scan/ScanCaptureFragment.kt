package io.falu.identity.capture.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import io.falu.identity.R
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.api.DocumentUploadDisposition
import io.falu.identity.api.models.DocumentSide
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.camera.CameraView
import io.falu.identity.capture.AbstractCaptureFragment
import io.falu.identity.capture.scan.utils.DocumentScanDisposition
import io.falu.identity.capture.scan.utils.ScanResult
import io.falu.identity.databinding.FragmentScanCaptureBinding

internal class ScanCaptureFragment(identityViewModelFactory: ViewModelProvider.Factory) :
    AbstractCaptureFragment(identityViewModelFactory) {
    private var _binding: FragmentScanCaptureBinding? = null
    private val binding get() = _binding!!

    private val documentScanViewModel: DocumentScanViewModel by activityViewModels()

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

        resetUI()

        binding.tvScanMessage.text = getString(
            R.string.scan_capture_text_scan_message,
            identityDocumentType?.getIdentityDocumentName(requireContext())
        )

        val inputStream = resources.openRawResource(R.raw.converted_model3)
        val file = identityViewModel.getModel(inputStream, "converted_model3.tflite")

        documentScanViewModel.initialize(file, 0.5f)
        startScan(identityDocumentType!!.getScanType().first)

        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_BACK

        binding.viewCamera.cameraViewType =
            if (identityDocumentType != IdentityDocumentType.PASSPORT) CameraView.CameraViewType.ID else CameraView.CameraViewType.PASSPORT

        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.isEnabled = false

        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationPage(it) },
            onError = {}
        )

        documentScanViewModel.documentScanDisposition.observe(viewLifecycleOwner) {
            updateUI(it)
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
        // noOp
    }

    private fun startScan(scanType: DocumentScanDisposition.DocumentScanType) {
        documentScanViewModel.scanner?.scan(binding.viewCamera.analyzers, scanType)
    }

    private fun resetUI() {
        binding.tvScanDocumentSide.text = getString(
            R.string.scan_capture_text_document_side_front,
            identityDocumentType?.getIdentityDocumentName(requireContext())
        )
    }

    private fun updateUI(result: ScanResult) {
        when (result.disposition) {
            is DocumentScanDisposition.Start -> {
                resetUI()
            }
            is DocumentScanDisposition.Detected -> {

            }
            is DocumentScanDisposition.Completed -> {

            }
            is DocumentScanDisposition.Timeout, null -> { //noOP
            }
        }
    }

    private fun onVerificationPage(verification: Verification) {
        documentScanViewModel.documentScanDisposition.observe(viewLifecycleOwner) {
            if (it.disposition is DocumentScanDisposition.Completed) {
                val output = it.output as DocumentDetectionOutput
                identityViewModel.uploadScannedDocument(
                    output.bitmap,
                    verification = verification.id,
                    scanType = it.disposition!!.type,
                    onError = {},
                    onFailure = {}
                )
            } else {
                // something else.
            }
        }
    }

    internal companion object {
        private fun IdentityDocumentType.getScanType(): Pair<DocumentScanDisposition.DocumentScanType, DocumentScanDisposition.DocumentScanType?> {
            return when (this) {
                IdentityDocumentType.IDENTITY_CARD -> {
                    Pair(
                        DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_FRONT,
                        DocumentScanDisposition.DocumentScanType.IDENTITY_DOCUMENT_BACK
                    )
                }
                IdentityDocumentType.PASSPORT -> {
                    Pair(
                        DocumentScanDisposition.DocumentScanType.PASSPORT,
                        null
                    )
                }
                IdentityDocumentType.DRIVING_LICENSE -> {
                    Pair(
                        DocumentScanDisposition.DocumentScanType.DL_FRONT,
                        DocumentScanDisposition.DocumentScanType.DL_BACK
                    )
                }
            }
        }
    }
}