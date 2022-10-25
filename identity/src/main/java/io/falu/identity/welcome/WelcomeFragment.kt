package io.falu.identity.welcome

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.databinding.FragmentWelcomeBinding
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.updateVerification
import software.tingle.api.HttpApiResponseProblem
import software.tingle.api.patch.JsonPatchDocument

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IdentityVerificationViewModel by activityViewModels()

    private lateinit var callback: IdentityVerificationResultCallback

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
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationSuccessful(it) },
            onError = { onVerificationFailure(it) })

        binding.buttonAccept.text = getString(R.string.welcome_button_accept)
        binding.buttonAccept.setOnClickListener {
            submitConsentData()
        }

        binding.buttonDecline.setOnClickListener {
            callback.onFinishWithResult(IdentityVerificationResult.Canceled)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onVerificationSuccessful(verification: Verification) {
        hideProgressView()
        binding.tvWelcomeSubtitle.text =
            getString(
                R.string.welcome_subtitle,
                verification.workspace.name.replaceFirstChar { it.uppercase() })
        binding.tvWelcomeBody.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onVerificationFailure(error: HttpApiResponseProblem?) {
        navigateToApiResponseProblemFragment(error)
    }

    private fun submitConsentData() {
        val document = JsonPatchDocument().replace("consent", true)
        binding.buttonAccept.showProgress()
        updateVerification(
            viewModel,
            document,
            onSuccess = {
                binding.buttonAccept.showProgress()
                findNavController().navigate(R.id.action_fragment_welcome_to_fragment_document_selection)
            })
    }

    private fun hideProgressView() {
        binding.progressView.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
        binding.viewButtons.visibility = View.VISIBLE
    }
}