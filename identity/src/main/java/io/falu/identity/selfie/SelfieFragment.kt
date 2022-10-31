package io.falu.identity.selfie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.databinding.FragmentSelfieBinding
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

        binding.buttonTakeSelfie.setOnClickListener {
            binding.viewCamera.takePhoto(
                onCaptured = {},
                onCaptureError = { navigateToErrorFragment(it) }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}