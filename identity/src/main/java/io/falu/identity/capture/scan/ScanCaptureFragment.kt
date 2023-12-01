package io.falu.identity.capture.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.DocumentDetectionOutput
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationCapture
import io.falu.identity.camera.CameraView
import io.falu.identity.capture.AbstractCaptureFragment.Companion.getIdentityDocumentName
import io.falu.identity.databinding.FragmentScanCaptureBinding
import io.falu.identity.documents.DocumentSelectionFragment
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.scan.ScanResult
import io.falu.identity.utils.FileUtils
import io.falu.identity.utils.serializable

internal class ScanCaptureFragment(identityViewModelFactory: ViewModelProvider.Factory) : Fragment() {

    private val identityViewModel: IdentityVerificationViewModel by activityViewModels { identityViewModelFactory }
    private val documentScanViewModel: DocumentScanViewModel by activityViewModels()

    private var _binding: FragmentScanCaptureBinding? = null
    private val binding get() = _binding!!

    private var scanType: ScanDisposition.DocumentScanType? = null
    private lateinit var identityDocumentType: IdentityDocumentType

    private lateinit var fileUtils: FileUtils

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
        identityDocumentType =
            requireArguments().serializable(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE)!!

        scanType =
            requireArguments().serializable(KEY_DOCUMENT_SCAN_TYPE) as? ScanDisposition.DocumentScanType

        fileUtils = FileUtils(requireContext())

        documentScanViewModel.resetScanDispositions()

        resetUI()

        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_BACK

        binding.viewCamera.cameraViewType =
            if (identityDocumentType != IdentityDocumentType.PASSPORT)
                CameraView.CameraViewType.ID
            else
                CameraView.CameraViewType.PASSPORT

        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationPage() },
            onError = {}
        )

        documentScanViewModel.documentScanDisposition.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        binding.buttonContinue.setOnClickListener {
            val result = binding.buttonContinue.tag as ScanResult
            when {
                scanType!!.isFront -> {
                    setFragmentResult(
                        REQUEST_KEY_DOCUMENT_SCAN,
                        bundleOf(KEY_SCAN_TYPE_FRONT to result.output)
                    )
                    findNavController().navigateUp()
                }

                scanType!!.isBack -> {
                    setFragmentResult(
                        REQUEST_KEY_DOCUMENT_SCAN,
                        bundleOf(KEY_SCAN_TYPE_BACK to result.output)
                    )
                    findNavController().navigateUp()
                }
            }
        }

        binding.buttonReset.setOnClickListener {
            resetUI()
            binding.viewScanResults.visibility = View.GONE
            binding.viewScan.visibility = View.VISIBLE
            documentScanViewModel.resetScanDispositions()
            val verification = binding.buttonReset.tag as Verification
            startScan(scanType!!, verification.capture)
            binding.viewCamera.startAnalyzer()
        }

        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onError = {},
            onSuccess = {
                binding.buttonReset.tag = it
                initiateScanner(it)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initiateScanner(verification: Verification) {
        identityViewModel.documentDetectorModelFile.observe(viewLifecycleOwner) {
            if (it != null) {
                documentScanViewModel.initialize(
                    it,
                    verification.capture.models.document.threshold
                )

                startScan(scanType!!, verification.capture)
            }
        }
    }

    private fun startScan(
        scanType: ScanDisposition.DocumentScanType,
        capture: VerificationCapture
    ) {
        documentScanViewModel.scanner?.scan(binding.viewCamera, scanType, capture)
    }

    private fun resetUI() {
        when {
            scanType!!.isFront -> {
                binding.tvScanDocumentSide.text = getString(
                    R.string.scan_capture_text_document_side_front,
                    identityDocumentType.getIdentityDocumentName(requireContext())
                )
            }

            scanType!!.isBack -> {
                binding.tvScanDocumentSide.text = getString(
                    R.string.scan_capture_text_document_side_back,
                    identityDocumentType.getIdentityDocumentName(requireContext())
                )
            }
        }

        binding.tvScanMessage.text = getString(
            R.string.scan_capture_text_scan_message,
            identityDocumentType.getIdentityDocumentName(requireContext())
        )
    }

    private fun updateUI(result: ScanResult?) {
        when (result?.disposition) {
            is ScanDisposition.Start -> {
                resetUI()
            }

            is ScanDisposition.Detected -> {
                binding.tvScanMessage.text = getString(R.string.scan_capture_text_document_detected)
            }

            is ScanDisposition.Desired -> {
                binding.tvScanMessage.text =
                    getString(R.string.scan_capture_text_document_scan_completed)
            }

            is ScanDisposition.Undesired -> {}
            is ScanDisposition.Completed -> {}
            is ScanDisposition.Timeout, null -> {
                // noOP
            }
        }
    }

    private fun onVerificationPage() {
        documentScanViewModel.documentScanCompleteDisposition.observe(viewLifecycleOwner) {
            if (it.disposition is ScanDisposition.Completed) {
                // stop the analyzer
                documentScanViewModel.scanner?.stopScan(binding.viewCamera)
                binding.buttonContinue.tag = it
                binding.buttonContinue.isEnabled = true

                binding.viewScan.visibility = View.GONE
                binding.viewScanResults.visibility = View.VISIBLE
                val output = it.output as DocumentDetectionOutput
                val bitmap = output.bitmap

                reportSuccessfulScanTelemetry(it.disposition as ScanDisposition.Completed, output)

                binding.ivScan.setImageBitmap(bitmap)
            } else if (it.disposition is ScanDisposition.Timeout) {

                identityViewModel.reportTelemetry(
                    identityViewModel
                        .analyticsRequestBuilder
                        .documentScanTimeOut(scanType = (it.disposition as ScanDisposition.Timeout).type)
                )

                documentScanViewModel.scanner?.stopScan(binding.viewCamera)
                findNavController().navigate(R.id.action_global_fragment_scan_capture_error)
            }
        }
    }

    private fun reportSuccessfulScanTelemetry(scanDisposition: ScanDisposition, output: DocumentDetectionOutput) {
        val telemetryDisposition = if (scanDisposition.type.isFront) {
            AnalyticsDisposition(frontModelScore = output.score, scanType = scanDisposition.type)
        } else {
            AnalyticsDisposition(backModelScore = output.score, scanType = scanDisposition.type)
        }

        identityViewModel.modifyAnalyticsDisposition(disposition = telemetryDisposition)
    }

    internal companion object {
        internal const val KEY_DOCUMENT_SCAN_TYPE = ":scan-type"
        internal const val KEY_SCAN_TYPE_FRONT = ":front"
        internal const val KEY_SCAN_TYPE_BACK = ":back"
        internal const val REQUEST_KEY_DOCUMENT_SCAN = ":scan"

        internal fun IdentityDocumentType.getScanType():
                Pair<ScanDisposition.DocumentScanType, ScanDisposition.DocumentScanType?> {
            return when (this) {
                IdentityDocumentType.IDENTITY_CARD -> {
                    Pair(
                        ScanDisposition.DocumentScanType.ID_FRONT,
                        ScanDisposition.DocumentScanType.ID_BACK
                    )
                }

                IdentityDocumentType.PASSPORT -> {
                    Pair(
                        ScanDisposition.DocumentScanType.PASSPORT,
                        null
                    )
                }

                IdentityDocumentType.DRIVING_LICENSE -> {
                    Pair(
                        ScanDisposition.DocumentScanType.DL_FRONT,
                        ScanDisposition.DocumentScanType.DL_BACK
                    )
                }
            }
        }
    }
}