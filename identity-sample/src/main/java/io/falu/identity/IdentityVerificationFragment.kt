package io.falu.identity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.falu.identity.databinding.FragmentIdentityVerificationBinding

class IdentityVerificationFragment : Fragment() {
    private var _binding: FragmentIdentityVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IdentityVerificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdentityVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStartVerification.setOnClickListener {
            startVerification(
                allowDrivingLicense = binding.cbAllowedTypeDl.isChecked,
                allowPassport = binding.cbAllowedTypePassport.isChecked,
                allowIdentityCard = binding.cbAllowedTypeId.isChecked,
                allowUploads = binding.cbAllowUploads.isChecked,
                requireLiveCapture = binding.cbRequireLiveCapture.isChecked
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startVerification(
        allowDrivingLicense: Boolean,
        allowPassport: Boolean,
        allowIdentityCard: Boolean,
        allowUploads: Boolean,
        requireLiveCapture: Boolean
    ) {
        binding.progressCircular.visibility = View.VISIBLE
        viewModel.requestIdentityVerification(
            allowDrivingLicense,
            allowPassport,
            allowIdentityCard,
            allowUploads,
            requireLiveCapture
        ).observe(viewLifecycleOwner) { result ->
            binding.progressCircular.visibility = View.VISIBLE

            if (result.successful() && result.resource != null) {
                val verification = result.resource!!
                CustomTabsIntent.Builder().build()
                    .launchUrl(
                        requireContext(),
                        Uri.parse(verification.url)
                    )
            }
        }
    }

    companion object {
        const val ALLOWED_TYPE_DRIVING_LICENSE = "driving_license"
        const val ALLOWED_TYPE_PASSPORT = "passport"
        const val ALLOWED_TYPE_ID_CARD = "id_card"
    }
}