package io.falu.identity.api.models.requirements

import androidx.annotation.IdRes
import io.falu.identity.R

internal data class RequirementError(
    var requirement: RequirementType?,
    var code: String,
    var description: String
)