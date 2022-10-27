package io.falu.identity.api.models.requirements

import androidx.annotation.IdRes
import io.falu.identity.R

internal data class RequirementError(
    var requirement: RequirementType,
    var code: String,
    var description: String,
) {
    internal companion object {
        private val document_upload_ids = arrayOf(
            R.id.action_fragment_document_capture_methods_to_fragment_upload_capture,
            R.id.action_fragment_document_capture_methods_to_fragment_upload_capture,
        )

        /**
         * Navigate to the fragment where the error was experienced.
         */
        fun RequirementError.canNavigateBackTo(@IdRes source: Int) =
            when (requirement) {
                RequirementType.CONSENT -> {
                    source == R.id.action_fragment_welcome_to_fragment_document_selection
                }
                RequirementType.COUNTRY,
                RequirementType.DOCUMENT_TYPE -> {
                    source == R.id.action_fragment_document_selection_to_fragment_document_capture_methods
                }
                RequirementType.DOCUMENT_FRONT,
                RequirementType.DOCUMENT_BACK -> {
                    document_upload_ids.contains(source)
                }
                RequirementType.SELFIE -> TODO()
                RequirementType.VIDEO -> TODO()
            }
    }
}