package io.falu.identity.api.models.requirements

internal data class RequirementError(
    var requirement: RequirementType,
    var code: String,
    var description: String,
)