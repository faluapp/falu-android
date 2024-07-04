package io.falu.identity.verification

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.core.exceptions.ApiException
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.verification.Gender
import io.falu.identity.api.models.verification.VerificationIdNumberUpload
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.api.models.verification.VerificationUploadRequest
import io.falu.identity.databinding.FragmentIdentificationVerificationBinding
import io.falu.identity.documents.DocumentSelectionFragment
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.serializable
import io.falu.identity.utils.showDatePickerDialog
import io.falu.identity.utils.showDialog
import io.falu.identity.utils.submitVerificationData
import io.falu.identity.utils.updateVerification
import java.util.Date

internal class IdentificationVerificationFragment(factory: ViewModelProvider.Factory) : Fragment() {

    private var _binding: FragmentIdentificationVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels { factory }

    private lateinit var identityDocumentType: IdentityDocumentType
    private var dateOfBirth: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdentificationVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityDocumentType = requireArguments()
            .serializable<IdentityDocumentType>(DocumentSelectionFragment.KEY_IDENTITY_DOCUMENT_TYPE)!!

        val genderAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_popup_item,
            Gender.entries.map { getString(it.desc) })

        binding.inputGender.setAdapter(genderAdapter)
        binding.inputGender.setText(genderAdapter.getItem(0), false)
        binding.viewBirthday.setOnClickListener {
            showDatePickerDialog {
                dateOfBirth = Date(it)
                binding.tvBirthday.text = DateUtils.formatDateTime(requireContext(), it, DateUtils.FORMAT_SHOW_DATE)
            }
        }
        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.setOnClickListener {
            verify()
        }
    }

    private fun verify() {
        val identityUpload = idNumberUpload ?: return
        val request = VerificationUploadRequest(idNumber = identityUpload)

        binding.buttonContinue.showProgress()
        updateVerification(request)
    }

    private fun updateVerification(request: VerificationUploadRequest) {
        val updateOptions = VerificationUpdateOptions(idNumber = request.idNumber)

        updateVerification(
            viewModel,
            updateOptions,
            0,
            onSuccess = { attemptIdNumberSubmission(verificationRequest = request) })
    }

    private fun attemptIdNumberSubmission(
        @IdRes source: Int = 0,
        verificationRequest: VerificationUploadRequest
    ) {
        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { verification ->
                when {
                    verification.taxPinRequired -> {
                        findNavController().navigate(
                            R.id.action_global_fragment_tax_pin_verification,
                            verificationRequest.addToBundle()
                        )
                    }

                    else -> {
                        submitVerificationData(viewModel, source, verificationRequest)
                    }
                }
            },
            onError = {
                navigateToApiResponseProblemFragment((it as ApiException).problem)
            }
        )
    }

    private val idNumberUpload: VerificationIdNumberUpload?
        get() {
            if (!isValidDocumentNumber) {
                binding.inputLayoutDocumentNumber.isErrorEnabled = true
                binding.inputLayoutDocumentNumber.error =
                    getString(R.string.document_verification_error_invalid_document_number)
                return null
            }
            binding.inputLayoutDocumentNumber.isErrorEnabled = false

            if (!isValidFirstName) {
                binding.inputLayoutFirstName.isErrorEnabled = true
                binding.inputLayoutFirstName.error = getString(R.string.document_verification_error_invalid_first_name)
                return null
            }
            binding.inputLayoutFirstName.isErrorEnabled = false

            if (!isValidLastName) {
                binding.inputLayoutLastName.isErrorEnabled = true
                binding.inputLayoutLastName.error = getString(R.string.document_verification_error_invalid_last_name)
                return null
            }
            binding.inputLayoutLastName.isErrorEnabled = false

            if (dateOfBirth == null) {
                requireContext().showDialog(
                    message = getString(R.string.document_verification_error_birthday),
                    positiveButton = Pair(getString(android.R.string.ok)) {}
                )
                return null
            }

            return VerificationIdNumberUpload(
                type = identityDocumentType,
                number = binding.inputDocumentNumber.text.toString(),
                firstName = binding.inputFirstName.text.toString(),
                lastName = binding.inputLastName.text.toString(),
                birthday = dateOfBirth ?: Date(),
                sex = binding.inputGender.text.toString().lowercase()
            )
        }

    private val isValidDocumentNumber: Boolean
        get() {
            val documentNumber = binding.inputDocumentNumber.text.toString()
            return documentNumber.isNotEmpty() && documentNumber.length > 5
        }

    private val isValidFirstName: Boolean
        get() {
            val firstName = binding.inputFirstName.text.toString()
            return firstName.isNotEmpty()
        }

    private val isValidLastName: Boolean
        get() {
            val firstName = binding.inputLastName.text.toString()
            return firstName.isNotEmpty()
        }
}