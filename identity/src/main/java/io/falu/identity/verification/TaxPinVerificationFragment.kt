package io.falu.identity.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.verification.VerificationTaxPinUpload
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.databinding.FragmentTaxPinVerificationBinding
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.updateVerification

internal class TaxPinVerificationFragment(factory: ViewModelProvider.Factory) : Fragment() {

    private var _binding: FragmentTaxPinVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxPinVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val verificationRequest =
            requireNotNull(VerificationUploadRequest.getFromBundle(requireArguments())) {
                "Verification upload request is null"
            }

        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.setOnClickListener {
            verify(verificationRequest)
        }
    }

    private fun verify(request: VerificationUploadRequest) {
        val taxPinUpload = taxPinUpload ?: return
        request.taxPin = taxPinUpload

        binding.buttonContinue.showProgress()
        updateVerification(request)
    }

    private fun updateVerification(request: VerificationUploadRequest) {
        val updateOptions = VerificationUpdateOptions(taxPin = request.taxPin)

        updateVerification(
            viewModel,
            updateOptions,
            0,
            onSuccess = { submitVerificationData(viewModel, 0, request) })
    }

    private val taxPinUpload: VerificationTaxPinUpload?
        get() {
            if (!isValidPin) {
                binding.inputLayoutTaxPin.isErrorEnabled = true
                binding.inputLayoutTaxPin.error =
                    getString(R.string.tax_pin_verification_error_invalid_tax_pin_number)
                return null
            }

            binding.inputLayoutTaxPin.isErrorEnabled = false

            val taxPin = binding.inputTaxPin.text.toString()

            return VerificationTaxPinUpload(value = taxPin)
        }

    private val isValidPin: Boolean
        get() {
            val taxPin = binding.inputTaxPin.text.toString()
            return taxPin.isNotEmpty()
        }
}