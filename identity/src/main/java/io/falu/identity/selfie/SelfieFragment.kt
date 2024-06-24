package io.falu.identity.selfie

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.core.exceptions.ApiException
import io.falu.core.models.FaluFile
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.FaceDetectionOutput
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationSelfieUpload
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraView
import io.falu.identity.databinding.FragmentSelfieBinding
import io.falu.identity.scan.ScanDisposition
import io.falu.identity.scan.ScanResult
import io.falu.identity.utils.getRenderScript
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.navigateToErrorFragment
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.updateVerification

internal class SelfieFragment(identityViewModelFactory: ViewModelProvider.Factory) : Fragment() {

    private val identityViewModel: IdentityVerificationViewModel by activityViewModels { identityViewModelFactory }
    private val faceScanViewModel: FaceScanViewModel by activityViewModels { faceScanViewModelFactory }

    private val faceScanViewModelFactory =
        FaceScanViewModel.factoryProvider(this) { identityViewModel.modelPerformanceMonitor }

    private var _binding: FragmentSelfieBinding? = null
    private val binding get() = _binding!!

    private lateinit var verificationRequest: VerificationUploadRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verificationRequest =
            requireNotNull(VerificationUploadRequest.getFromBundle(requireArguments())) {
                "Verification upload request is null"
            }

        faceScanViewModel.resetScanDispositions()
        resetUI()

        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_FRONT
        binding.viewCamera.cameraViewType = CameraView.CameraViewType.FACE
        binding.buttonContinue.text = getString(R.string.button_continue)

        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onError = {},
            onSuccess = {
                binding.buttonReset.tag = it
                initiateAnalyzer(it)
            }
        )

        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationPage() },
            onError = {}
        )

        binding.buttonReset.setOnClickListener {
            binding.viewSelfieCamera.visibility = View.VISIBLE
            binding.viewSelfieResult.visibility = View.GONE
            resetUI()
            faceScanViewModel.resetScanDispositions()
            val verification = binding.buttonReset.tag as Verification
            scan(verification)
            binding.viewCamera.startAnalyzer()
        }

        faceScanViewModel.faceScanDisposition.observe(viewLifecycleOwner) {
            updateUI(it)
        }
    }

    private fun initiateAnalyzer(verification: Verification) {
        identityViewModel.faceDetectorModelFile.observe(viewLifecycleOwner) {
            if (it != null) {
                faceScanViewModel
                    .initialize(it, verification.capture.models.face?.threshold ?: THRESHOLD)
            }

            scan(verification)
        }
    }

    private fun scan(verification: Verification) {
        faceScanViewModel.scanner?.scan(
            binding.viewCamera,
            ScanDisposition.DocumentScanType.SELFIE,
            verification.capture,
            requireContext().getRenderScript()
        )
    }

    private fun bindToUI(output: FaceDetectionOutput) {
        binding.viewSelfieCamera.visibility = View.GONE
        binding.viewSelfieResult.visibility = View.VISIBLE
        binding.buttonContinue.visibility = View.VISIBLE

        binding.ivSelfie.setImageBitmap(output.bitmap)

        binding.buttonContinue.setOnClickListener {
            binding.buttonContinue.showProgress()
            uploadSelfie(output.bitmap)
        }
    }

    private fun updateUI(result: ScanResult) {
        when (result.disposition) {
            is ScanDisposition.Start -> {
                resetUI()
            }

            is ScanDisposition.Detected -> {
                binding.tvScanMessage.text = getString(R.string.selfie_text_face_detected)
            }

            is ScanDisposition.Desired -> {
                binding.tvScanMessage.text =
                    getString(R.string.selfie_text_selfie_scan_completed)
            }

            is ScanDisposition.Undesired -> {}
            is ScanDisposition.Completed -> {}
            is ScanDisposition.Timeout, null -> {
                // noOP
            }
        }
    }

    private fun resetUI() {
        binding.tvScanMessage.text = getString(R.string.selfie_text_scan_message)
    }

    private fun uploadSelfie(bitmap: Bitmap) {
        identityViewModel.uploadSelfieImage(
            bitmap,
            onSuccess = { submitSelfieAndUploadedDocuments(it) },
            onFailure = { navigateToErrorFragment(it) },
            onError = { navigateToApiResponseProblemFragment((it as ApiException).problem) }
        )
    }

    private fun reportCameraInfoTelemetry(cameraInfo: CameraInfo) {
        identityViewModel.reportTelemetry(
            identityViewModel.analyticsRequestBuilder.cameraInfo(cameraInfo.sensorRotationDegrees)
        )
    }

    private fun submitSelfieAndUploadedDocuments(file: FaluFile) {
        val selfie = VerificationSelfieUpload(
            UploadMethod.MANUAL,
            file = file.id,
            variance = 0F
        )

        val updateOptions = VerificationUpdateOptions(selfie = selfie)

        updateVerification(identityViewModel, updateOptions, R.id.fragment_selfie, onSuccess = {
            selfie.camera = binding.viewCamera.cameraSettings
            verificationRequest.selfie = selfie
            submitVerificationData(identityViewModel, R.id.fragment_selfie, verificationRequest)
        })
    }

    private fun onVerificationPage() {
        faceScanViewModel.faceScanCompleteDisposition.observe(viewLifecycleOwner) {
            if (it.disposition is ScanDisposition.Completed) {
                // stop the analyzer
                binding.viewCamera.stopAnalyzer()
                binding.viewCamera.analyzers.clear()

                val output = it.output as FaceDetectionOutput
                reportFaceScanSuccessfulTelemetry(output = output)
                bindToUI(output)
            } else if (it.disposition is ScanDisposition.Timeout) {
                identityViewModel.reportTelemetry(identityViewModel.analyticsRequestBuilder.selfieScanTimeOut())

                binding.viewCamera.stopAnalyzer()
                binding.viewCamera.analyzers.clear()

                findNavController().navigate(R.id.action_global_fragment_selfie_capture_error)
            }
        }
    }

    private fun reportFaceScanSuccessfulTelemetry(output: FaceDetectionOutput) {
        identityViewModel.modifyAnalyticsDisposition(
            disposition = AnalyticsDisposition(selfieModelScore = output.score)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val THRESHOLD = 0.75f
    }
}