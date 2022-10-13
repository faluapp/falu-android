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
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.databinding.FragmentWelcomeBinding
import io.falu.identity.utils.updateVerification
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.patch.JsonPatchDocument

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

        binding.buttonAccept.setOnClickListener {
            submitConsentData(true)
        }

        binding.buttonDecline.setOnClickListener {
            submitConsentData(false)
        }
    }

    private fun onVerificationSuccessful(verification: Verification) {
        binding.tvWelcomeSubtitle.text =
            getString(
                R.string.welcome_subtitle,
                verification.workspace.name.replaceFirstChar { it.uppercase() })
        binding.tvWelcomeBody.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onVerificationFailure(error: HttpApiResponseProblem?) {
        // TODO: Redirect to error fragment
    }

    private fun submitConsentData(accepted: Boolean) {
        val document = JsonPatchDocument().replace("consent", accepted)
        binding.progressCircular.visibility = View.VISIBLE
        updateVerification(viewModel, document, onSuccess = {
            binding.progressCircular.visibility = View.GONE
            findNavController().navigate(R.id.action_fragment_welcome_to_fragment_document_selection)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}