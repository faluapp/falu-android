package io.falu.identity.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationViewModel
import io.falu.identity.R
import io.falu.identity.analytics.IdentityAnalyticsRequestBuilder.Companion.SCREEN_NAME_DOCUMENT_SELECTION
import io.falu.identity.api.models.IdentityDocumentType
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.api.models.verification.VerificationType
import io.falu.identity.api.models.verification.VerificationUpdateOptions
import io.falu.identity.countries.CountriesAdapter
import io.falu.identity.databinding.FragmentDocumentSelectionBinding
import io.falu.identity.utils.navigateToApiResponseProblemFragment
import io.falu.identity.utils.updateVerification

class DocumentSelectionFragment(private val factory: ViewModelProvider.Factory) : Fragment() {

    private val viewModel: IdentityVerificationViewModel by activityViewModels { factory }

    private var _binding: FragmentDocumentSelectionBinding? = null
    private val binding get() = _binding!!

    private var identityDocumentType: IdentityDocumentType? = null

    private var verification: Verification? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.reportTelemetry(viewModel.analyticsRequestBuilder
            .screenPresented(screenName = SCREEN_NAME_DOCUMENT_SELECTION))

        viewModel.observerForSupportedCountriesResults(
            viewLifecycleOwner,
            onSuccess = { onSupportedCountriesListed(it.toList()) },
            onError = {
                navigateToApiResponseProblemFragment(it)
            }
        )

        binding.buttonContinue.text = getString(R.string.button_continue)
        binding.buttonContinue.isEnabled = false
        binding.buttonContinue.setOnClickListener {
            updateVerification()
        }

        binding.groupDocumentTypes.setOnCheckedStateChangeListener { group, checkIds ->
            binding.buttonContinue.isEnabled = checkIds.isNotEmpty()

            when (group.checkedChipId) {
                R.id.chip_passport -> identityDocumentType = IdentityDocumentType.PASSPORT
                R.id.chip_identity_card -> identityDocumentType = IdentityDocumentType.IDENTITY_CARD
                R.id.chip_driving_license -> identityDocumentType = IdentityDocumentType.DRIVING_LICENSE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     *
     */
    private fun onSupportedCountriesListed(countries: List<SupportedCountry>) {
        val countriesAdapter = CountriesAdapter(requireContext(), R.layout.list_item_countries, countries)

//            ArrayAdapter(
//                requireContext(),
//                R.layout.dropdown_menu_popup_item,
//                countries.map { it.country.name })
        binding.inputIssuingCountry.setAdapter(countriesAdapter)
        binding.inputIssuingCountry.setText(countriesAdapter.getItem(0)?.country?.name.orEmpty(), false)

        val country = getSupportedCountry(countries)
        getVerificationResults(country)

        binding.inputIssuingCountry.setOnItemClickListener { _, _, position, _ ->
            binding.inputIssuingCountry.setText(countriesAdapter.getItem(position)?.country?.name.orEmpty(), false)
            getVerificationResults(country)
        }
    }

    /**
     *
     */
    private fun getSupportedCountry(countries: List<SupportedCountry>): SupportedCountry {
        val country = binding.inputIssuingCountry.text.toString()
        return countries.first { it.country.name == country }
    }

    /**
     *
     */
    private fun getVerificationResults(country: SupportedCountry) {
        binding.buttonContinue.tag = country
        viewModel.observeForVerificationResults(
            viewLifecycleOwner,
            onSuccess = { acceptedDocumentOptions(it, country) },
            onError = {}
        )
    }

    /***/
    private fun updateVerification() {
        verification?.let { updateVerification(it) }
    }

    /***/
    private fun updateVerification(verification: Verification) {
        binding.buttonContinue.showProgress()
        val country = binding.buttonContinue.tag as SupportedCountry
        val updateOptions = VerificationUpdateOptions(country = country.country.code)

        val action = if (verification.type != VerificationType.IDENTITY_NUMBER) {
            R.id.action_fragment_document_selection_to_fragment_document_capture_methods
        } else {
            R.id.action_fragment_document_selection_to_fragment_identity_verification
        }

        updateVerification(
            viewModel,
            updateOptions,
            source = R.id.action_fragment_welcome_to_fragment_document_selection,
            onSuccess = {
                val bundle = bundleOf(KEY_IDENTITY_DOCUMENT_TYPE to identityDocumentType)
                findNavController().navigate(action, bundle)
            })
    }

    /**
     *
     */
    private fun acceptedDocumentOptions(verification: Verification, country: SupportedCountry) {
        this.verification = verification

        val acceptedDocuments =
            verification.options.document.allowed.toSet().intersect(country.documents.toSet())

        binding.chipIdentityCard.isEnabled =
            acceptedDocuments.contains(IdentityDocumentType.IDENTITY_CARD)
        binding.chipPassport.isEnabled =
            acceptedDocuments.contains(IdentityDocumentType.PASSPORT)
        binding.chipDrivingLicense.isEnabled =
            acceptedDocuments.contains(IdentityDocumentType.DRIVING_LICENSE)
    }

    internal companion object {
        const val KEY_IDENTITY_DOCUMENT_TYPE = ":document-type"
    }
}