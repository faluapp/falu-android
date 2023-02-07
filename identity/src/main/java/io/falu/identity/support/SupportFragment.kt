package io.falu.identity.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.api.models.Support
import io.falu.identity.databinding.FragmentSupportBinding
import io.falu.identity.utils.navigateToApiResponseProblemFragment


internal class SupportFragment(
    private val factory: ViewModelProvider.Factory,
) : Fragment() {
    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = {
                val support = it.support!!
                bindSupportViews(support)
            },
            onError = {
                navigateToApiResponseProblemFragment(it)
            }
        )
    }

    private fun bindSupportViews(support: Support) {
        binding.tvSupportUrl.text = support.url
        binding.viewSupportCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${support.phone}")
            startActivity(intent)
        }
        binding.viewSupportEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(support.email))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}