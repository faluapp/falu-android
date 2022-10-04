package io.falu.identity.welcome

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.Verification
import io.falu.identity.databinding.FragmentWelcomeBinding
import software.tingle.api.HttpApiResponseProblem

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationSuccessful(it) },
            onFailure = { onVerificationFailure(it) })


        binding.tvWelcomeBody.movementMethod = LinkMovementMethod.getInstance()

        binding.buttonGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_welcome_to_fragment_document_selection)
        }
    }

    private fun onVerificationSuccessful(verification: Verification) {
        binding.buttonGetStarted.isEnabled = true
        binding.tvWelcomeSubtitle.text =
            getString(R.string.welcome_subtitle, verification.workspace.name)
        binding.tvWelcomeBody.text = getString(
            R.string.welcome_body,
            verification.business.name,
        )
        binding.tvWelcomeBody.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun onVerificationFailure(error: HttpApiResponseProblem?) {
        binding.buttonGetStarted.isEnabled = false
        // TODO: Redirect to error fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}