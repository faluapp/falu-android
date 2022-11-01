package io.falu.identity.selfie

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.databinding.FragmentSelfieBinding
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.navigateToErrorFragment

class SelfieFragment : Fragment() {
    private val viewModel: IdentityVerificationViewModel by activityViewModels()

    private var _binding: FragmentSelfieBinding? = null
    private val binding get() = _binding!!

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
        binding.viewCamera.lifecycleOwner = viewLifecycleOwner
        binding.viewCamera.lensFacing = CameraSelector.LENS_FACING_FRONT

        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.isEnabled = false

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
            uploadSelfie(selfieUri)
        }
    }

    private fun uploadSelfie(uri: Uri) {
        viewModel.uploadSelfieImage(
            uri,
            onFailure = { navigateToErrorFragment(it) },
            onError = { navigateToApiResponseProblemFragment(it) },
        )
    }

    private fun submitSelfieAndUploadedDocuments() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}