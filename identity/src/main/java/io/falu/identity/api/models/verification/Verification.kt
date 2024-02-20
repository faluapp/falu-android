package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName
import io.falu.identity.api.models.Branding
import io.falu.identity.api.models.BusinessInfo
import io.falu.identity.api.models.Support
import io.falu.identity.api.models.WorkspaceInfo
import io.falu.identity.api.models.requirements.Requirement

internal data class Verification(
    /**
     * Unique identifier of the identity verification
     */
    var id: String,
    var type: VerificationType,
    var status: VerificationStatus,
    var options: VerificationOptions,
    /**
     * The fallback url, used when there are unsupported features
     */
    var url: String?,
    /**
     * Contains `true` if verification is in live mode and `false` if it isn't
     */
    var live: Boolean,
    @SerializedName("remaining_attempts")
    var remainingAttempts: Int? = null,
    var workspace: WorkspaceInfo,
    var business: BusinessInfo,
    var support: Support?,
    var branding: Branding?,
    var requirements: Requirement,
    var capture: VerificationCapture,
    var supported: Boolean = true
) {
    val selfieRequired: Boolean
        get() = options.selfie != null

    val videoRequired: Boolean
        get() = options.video != null

    val hasRequirementErrors: Boolean
        get() = requirements.errors.isNotEmpty()

    val submitted: Boolean
        get() = status == VerificationStatus.PROCESSING
}