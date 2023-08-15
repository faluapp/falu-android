package io.falu.identity.api.models.requirements

import androidx.annotation.IdRes
import io.falu.identity.R

internal data class RequirementError(
    var requirement: RequirementType?,
    var code: String,
    var description: String
) {
    internal companion object {
        private val document_upload_ids = arrayOf(
            R.id.fragment_manual_capture,
            R.id.fragment_upload_capture
        )

        /**
         * Navigate to the fragment where the error was experienced.
         */
        fun RequirementError.canNavigateBackTo(@IdRes source: Int) =
            when (requirement) {
                RequirementType.CONSENT -> {
                    source == R.id.fragment_welcome
                }
                RequirementType.COUNTRY,
                RequirementType.DOCUMENT_TYPE -> {
                    source == R.id.fragment_document_selection
                }
                RequirementType.DOCUMENT_FRONT,
                RequirementType.DOCUMENT_BACK -> {
                    document_upload_ids.contains(source)
                }
                RequirementType.SELFIE -> TODO()
                RequirementType.VIDEO -> TODO()
                else -> false
            }
    }
}