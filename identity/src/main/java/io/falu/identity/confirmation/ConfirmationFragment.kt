package io.falu.identity.confirmation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.databinding.FragmentConfirmationBinding

internal class ConfirmationFragment : Fragment() {
    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    @VisibleForTesting
    internal lateinit var callback: IdentityVerificationResultCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as IdentityVerificationResultCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ${IdentityVerificationResultCallback::class.java}")
        }
    }

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
            callback.onFinishWithResult(IdentityVerificationResult.Succeeded)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}