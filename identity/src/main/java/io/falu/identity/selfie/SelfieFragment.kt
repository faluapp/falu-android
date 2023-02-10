package io.falu.identity.selfie

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import io.falu.core.models.FaluFile
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.ai.FaceDetectionAnalyzer
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationSelfieUpload
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.camera.CameraView
import io.falu.identity.databinding.FragmentSelfieBinding
import io.falu.identity.utils.*
import software.tingle.api.patch.JsonPatchDocument

internal class SelfieFragment(identityViewModelFactory: ViewModelProvider.Factory) : Fragment() {

    private val identityViewModel: IdentityVerificationViewModel by activityViewModels { identityViewModelFactory }

    private var _binding: FragmentSelfieBinding? = null
    private val binding get() = _binding!!

    private lateinit var verificationRequest: VerificationUploadRequest
    private lateinit var analyzer: FaceDetectionAnalyzer

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

        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_FRONT
        binding.viewCamera.cameraViewType = CameraView.CameraViewType.FACE
        binding.buttonContinue.text = getString(R.string.button_continue)

        identityViewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onError = {},
            onSuccess = {
                binding.buttonTakeSelfie.tag = it
                initiateAnalyzer(it)
            }
        )

        binding.buttonTakeSelfie.setOnClickListener {
            val verification = binding.buttonTakeSelfie.tag as Verification
            binding.viewCamera.takePhoto(
                onCaptured = { analyzeImage(it, verification) },
                onCaptureError = { navigateToErrorFragment(it) }
            )
        }

        binding.buttonReset.setOnClickListener {
            binding.viewSelfieCamera.visibility = View.VISIBLE
            binding.viewSelfieResult.visibility = View.GONE
        }
    }

    private fun initiateAnalyzer(verification: Verification) {
        identityViewModel.faceDetectorModelFile.observe(viewLifecycleOwner) {
            if (it != null) {
                analyzer = FaceDetectionAnalyzer(
                    it,
                    verification.capture.models.face?.threshold ?: THRESHOLD
                )
            }
        }
    }

    private fun bindToUI(uri: Uri) {
        binding.viewSelfieCamera.visibility = View.GONE
        binding.viewSelfieResult.visibility = View.VISIBLE
        binding.viewSelfieError.visibility = View.GONE
        binding.buttonContinue.visibility = View.VISIBLE
        binding.cvSelfie.visibility = View.VISIBLE

        binding.ivSelfie.setImageURI(uri)

        binding.buttonContinue.setOnClickListener {
            binding.buttonContinue.showProgress()
            uploadSelfie(uri)
        }
    }

    private fun bindToUIWithError() {
        binding.viewSelfieCamera.visibility = View.GONE
        binding.viewSelfieResult.visibility = View.VISIBLE
        binding.viewSelfieError.visibility = View.VISIBLE
        binding.cvSelfie.visibility = View.GONE
        binding.buttonContinue.visibility = View.GONE
    }

    private fun uploadSelfie(uri: Uri) {
        identityViewModel.uploadSelfieImage(
            uri,
            onSuccess = { submitSelfieAndUploadedDocuments(it) },
            onFailure = { navigateToErrorFragment(it) },
            onError = { navigateToApiResponseProblemFragment(it) },
        )
    }

    private fun submitSelfieAndUploadedDocuments(file: FaluFile) {
        val selfie =
            VerificationSelfieUpload(
                UploadMethod.MANUAL,
                file = file.id,
                variance = 0F,
            )

        val document = JsonPatchDocument().replace("/selfie", selfie)

        updateVerification(identityViewModel, document, R.id.fragment_selfie, onSuccess = {
            selfie.camera = binding.viewCamera.cameraSettings
            verificationRequest.selfie = selfie
            submitVerificationData(identityViewModel, R.id.fragment_selfie, verificationRequest)
        })
    }

    private fun analyzeImage(uri: Uri?, verification: Verification) {
        val selfieUri = requireNotNull(uri) {
            "Selfie uri is null"
        }

        val output = analyzer.analyze(
            selfieUri.toBitmap(requireContext().contentResolver)
        )

        val threshold = verification.capture.models.face?.threshold ?: THRESHOLD

        if (output.score >= threshold) {
            bindToUI(selfieUri)
            return
        }

        // show selfie capture error results.
        bindToUIWithError()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val THRESHOLD = 0.75f
    }
}