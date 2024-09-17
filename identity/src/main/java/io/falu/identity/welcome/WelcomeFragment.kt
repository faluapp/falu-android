package io.falu.identity.welcome

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.core.exceptions.ApiException
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.IdentityVerificationResultCallback
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.AnalyticsDisposition
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_WELCOME
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.databinding.FragmentWelcomeBinding
import io.falu.identity.error.ErrorFragment.Companion.navigateWithDepletedAttempts
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.updateVerification
import software.tingle.api.HttpApiResponseProblem

internal class WelcomeFragment(
    private val factory: ViewModelProvider.Factory,
    private val callback: IdentityVerificationResultCallback
) :
    Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels { factory }

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
        viewModel.reportTelemetry(viewModel.analyticsRequestBuilder.screenPresented(screenName = SCREEN_NAME_WELCOME))
        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { onVerificationSuccessful(it) },
            onError = { onVerificationFailure((it as ApiException).problem) })

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
        viewModel.modifyAnalyticsDisposition(disposition = AnalyticsDisposition(selfie = verification.selfieRequired))

        hideProgressView()

        val remainingAttempts = verification.remainingAttempts

        if (remainingAttempts != null && remainingAttempts == 0) {
            findNavController().navigateWithDepletedAttempts(requireContext())
            return
        }

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
        val updateOptions = VerificationUpdateOptions(consent = true)

        binding.buttonAccept.showProgress()
        updateVerification(
            viewModel,
            updateOptions,
            source = R.id.fragment_welcome,
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