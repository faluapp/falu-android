package io.falu.identity.error

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.falu.identity.IdentityVerificationResult
import io.falu.identity.R

internal class SelfieCaptureErrorFragment(identityViewModelFactory: ViewModelProvider.Factory) :
    AbstractErrorFragment(identityViewModelFactory) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvErrorTitle.text = getString(R.string.error_title_selfie_capture)
        binding.tvErrorDescription.text = getString(R.string.error_description_selfie_capture)

        binding.buttonErrorActionPrimary.text = getString(R.string.button_try_again)

        binding.buttonErrorActionSecondary.visibility = View.VISIBLE
        binding.buttonErrorActionSecondary.text = getString(R.string.button_cancel)

        binding.buttonErrorActionPrimary.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonErrorActionSecondary.setOnClickListener {
            callback.onFinishWithResult(IdentityVerificationResult.Canceled)
        }
    }
}