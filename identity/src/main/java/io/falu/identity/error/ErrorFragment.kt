package io.falu.identity.error

import android.os.Bundle
import android.view.View
import io.falu.identity.R
import io.falu.identity.utils.KEY_ERROR_CAUSE

internal class ErrorFragment : AbstractErrorFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val throwable = requireArguments().getSerializable(KEY_ERROR_CAUSE) as Throwable

        binding.tvErrorTitle.text = getString(R.string.error_title)
        binding.tvErrorDescription.text = getString(R.string.error_description_server)
        binding.tvErrorMessage.text = throwable.message
    }
}