package io.falu.identity

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import io.falu.identity.databinding.FragmentIdentityVerificationBinding
import io.falu.identity.models.IdentityVerificationCreationRequest
import io.falu.identity.models.IdentityVerificationOptions
import io.falu.identity.models.IdentityVerificationOptionsForDocument

class IdentityVerificationFragment : Fragment() {
    private var _binding: FragmentIdentityVerificationBinding? = null
    private val binding get() = _binding!!

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
//            startVerification(
//                allowDrivingLicense = binding.cbAllowedTypeDl.isChecked,
//                allowPassport = binding.cbAllowedTypePassport.isChecked,
//                allowIdentityCard = binding.cbAllowedTypeId.isChecked,
//                allowUploads = binding.cbAllowUploads.isChecked,
//                requireLiveCapture = binding.cbRequireLiveCapture.isChecked
//            )
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
        val request = IdentityVerificationCreationRequest(
            options = IdentityVerificationOptions(
                allowUploads = allowUploads,
                document = IdentityVerificationOptionsForDocument(
                    live = requireLiveCapture,
                    allowed = mutableListOf<String>().also {
                        if (allowDrivingLicense) it.add(ALLOWED_TYPE_DRIVING_LICENSE)
                        if (allowPassport) it.add(ALLOWED_TYPE_PASSPORT)
                        if (allowIdentityCard) it.add(ALLOWED_TYPE_ID_CARD)
                    }
                )
            ),
            type = "document"
        )

        binding.progressCircular.visibility = View.VISIBLE

    }

    companion object {
        const val ALLOWED_TYPE_DRIVING_LICENSE = "driving_license"
        const val ALLOWED_TYPE_PASSPORT = "passport"
        const val ALLOWED_TYPE_ID_CARD = "id_card"
    }
}