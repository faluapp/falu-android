package io.falu.identity.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import io.falu.identity.IdentityVerificationActivity
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.databinding.FragmentConfirmationBinding

internal class ConfirmationFragment : Fragment() {
    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFinish.setOnClickListener {
            setFragmentResult(
                IdentityVerificationActivity.REQUEST_KEY_IDENTITY_VERIFICATION_RESULT,
                IdentityVerificationResult.Succeeded.addToBundle()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}