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
import io.falu.identity.api.models.UploadMethod
import io.falu.identity.api.models.verification.VerificationSelfieUpload
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.databinding.FragmentSelfieBinding
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.navigateToErrorFragment
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.updateVerification
import software.tingle.api.patch.JsonPatchDocument

class SelfieFragment(identityViewModelFactory: ViewModelProvider.Factory) : Fragment() {

    private val identityViewModel: IdentityVerificationViewModel by activityViewModels { identityViewModelFactory }

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

        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_FRONT
        binding.buttonContinue.text = getString(R.string.button_continue)

        binding.buttonTakeSelfie.setOnClickListener {
            binding.viewCamera.takePhoto(
                onCaptured = { bindToUI(it) },
                onCaptureError = { navigateToErrorFragment(it) }
            )
        }

        binding.buttonReset.setOnClickListener {
            binding.viewSelfieCamera.visibility = View.VISIBLE
            binding.viewSelfieResult.visibility = View.GONE
        }
    }

    private fun bindToUI(uri: Uri?) {
        val selfieUri = requireNotNull(uri) {
            "Selfie uri is null"
        }
        binding.viewSelfieCamera.visibility = View.GONE
        binding.viewSelfieResult.visibility = View.VISIBLE
        binding.ivSelfie.setImageURI(selfieUri)

        binding.buttonContinue.setOnClickListener {
            binding.buttonContinue.showProgress()
            uploadSelfie(selfieUri)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}