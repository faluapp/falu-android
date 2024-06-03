package io.falu.identity

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.falu.identity.sample.R
import io.falu.identity.sample.databinding.FragmentIdentityVerificationBinding

class IdentityVerificationFragment : Fragment(), IdentityVerificationCallback {
    private var _binding: FragmentIdentityVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VerificationViewModel by activityViewModels()
    private lateinit var verificationView: FaluIdentityVerificationView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdentityVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val logoUri: Uri
        get() = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.logo_no_text))
            .appendPath(resources.getResourceTypeName(R.drawable.logo_no_text))
            .appendPath(resources.getResourceEntryName(R.drawable.logo_no_text))
            .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verificationView = FaluIdentityVerificationView.create(
            fragment = this,
            logo = logoUri,
            // logo = Uri.parse("https://path/to/logo-no-text.jpg") // Or use a remote image
            callback = this
        )

        binding.buttonStartVerification.setOnClickListener {
            startVerification(
                allowDrivingLicense = binding.swAllowedTypeDl.isChecked,
                allowPassport = binding.swAllowedTypePassport.isChecked,
                allowIdentityCard = binding.swAllowedTypeId.isChecked,
                allowUploads = binding.swAllowUploads.isChecked,
                allowDocumentSelfie = binding.swAllowDocumentSelfie.isChecked,
                allowIdNumberVerification = binding.swAllowIdNumberVerification.isChecked
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
        allowDocumentSelfie: Boolean,
        allowIdNumberVerification: Boolean
    ) {
        binding.viewProgress.visibility = View.VISIBLE
        viewModel.requestIdentityVerification(
            allowDrivingLicense,
            allowPassport,
            allowIdentityCard,
            allowUploads,
            allowDocumentSelfie,
            allowIdNumberVerification
        ).observe(viewLifecycleOwner) { result ->
            binding.viewProgress.visibility = View.GONE

            if (result.successful() && result.resource != null) {
                val verification = result.resource!!
                if (binding.swVerificationOption.isChecked) {
                    verificationView.open(verification.id, verification.temporaryKey)
                } else {
                    CustomTabsIntent.Builder().build()
                        .launchUrl(
                            requireContext(),
                            Uri.parse(verification.url)
                        )
                }
            }
        }
    }

    override fun onVerificationResult(result: IdentityVerificationResult) {
        when (result) {
            IdentityVerificationResult.Succeeded -> {
                binding.tvResult.text = "Processing"
            }

            IdentityVerificationResult.Canceled -> {
                binding.tvResult.text = "Canceled"
            }

            is IdentityVerificationResult.Failed -> {
                binding.tvResult.text = "Failed : ${result.throwable}"
            }
        }
    }
}