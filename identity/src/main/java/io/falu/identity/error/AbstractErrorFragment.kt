package io.falu.identity.error

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.databinding.FragmentErrorBinding

internal abstract class AbstractErrorFragment : Fragment() {
    private var _binding: FragmentErrorBinding? = null
    protected val binding get() = _binding!!

    protected lateinit var callback: IdentityVerificationResultCallback

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
    ): View? {
        _binding = FragmentErrorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}