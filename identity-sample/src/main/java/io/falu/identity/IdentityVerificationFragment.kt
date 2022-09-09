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
import io.falu.identity.databinding.FragmentIdentityVerificationBinding

class IdentityVerificationFragment : Fragment(), IdentityVerificationResultCallback {
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

    private val logoUri: Uri
        get() = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.mipmap.ic_launcher))
            .appendPath(resources.getResourceTypeName(R.mipmap.ic_launcher))
            .appendPath(resources.getResourceEntryName(R.mipmap.ic_launcher))
            .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val verificationView = FaluIdentityVerificationView.create(
            fragment = this,
            logo = logoUri,
            callback = this
        )
        binding.buttonStartVerification.setOnClickListener {
            verificationView.open("", "")
//            startVerification(
//                allowDrivingLicense = binding.swAllowedTypeDl.isChecked,
//                allowPassport = binding.swAllowedTypePassport.isChecked,
//                allowIdentityCard = binding.swAllowedTypeId.isChecked,
//                allowUploads = binding.swAllowUploads.isChecked,
//                allowDocumentSelfie = binding.swAllowDocumentSelfie.isChecked,
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
        allowDocumentSelfie: Boolean
    ) {
        binding.viewProgress.visibility = View.VISIBLE
        viewModel.requestIdentityVerification(
            allowDrivingLicense,
            allowPassport,
            allowIdentityCard,
            allowUploads,
            allowDocumentSelfie
        ).observe(viewLifecycleOwner) { result ->
            binding.viewProgress.visibility = View.GONE

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

    override fun onVerificationResult(result: IdentityVerificationResult?) {

    }
}