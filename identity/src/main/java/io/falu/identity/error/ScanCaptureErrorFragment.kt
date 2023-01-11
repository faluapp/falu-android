package io.falu.identity.error

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import io.falu.identity.R

internal class ScanCaptureErrorFragment : AbstractErrorFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvErrorTitle.text = getString(R.string.error_title_scan_capture)
        binding.tvErrorDescription.text = getString(R.string.error_description_scan_capture)
        binding.tvErrorMessage.text = getString(R.string.error_message_scan_capture)

        binding.buttonErrorActionPrimary.text = getString(R.string.button_try_again)

        binding.buttonErrorActionSecondary.visibility = View.VISIBLE
        binding.buttonErrorActionSecondary.text = getString(R.string.button_change_method)

        binding.buttonErrorActionPrimary.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonErrorActionSecondary.setOnClickListener {
            findNavController().popBackStack(R.id.fragment_document_capture_methods, true)
        }
    }
}