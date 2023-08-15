package io.falu.identity.api.models.requirements

internal data class Requirement(
    var errors: MutableList<RequirementError>,
    var pending: MutableList<RequirementType>
)