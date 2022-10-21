package io.falu.identity.error

import android.os.Bundle
import android.view.View
import io.falu.identity.R
import io.falu.identity.utils.KEY_ERROR_DESCRIPTION

internal class ApiResponseProblemFragment : AbstractErrorFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val description = requireArguments().getString(KEY_ERROR_DESCRIPTION)

        binding.tvErrorTitle.text = getString(R.string.error_title_http_error)
        binding.tvErrorDescription.text = description
    }
}