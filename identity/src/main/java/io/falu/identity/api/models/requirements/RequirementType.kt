package io.falu.identity.api.models.requirements

import com.google.gson.annotations.SerializedName
import io.falu.identity.IdentityVerificationNavActions
import io.falu.identity.api.models.verification.Verification

internal enum class RequirementType {
    @SerializedName("consent")
    CONSENT,

    @SerializedName("country")
    COUNTRY,

    @SerializedName("document_type")
    DOCUMENT_TYPE,

    @SerializedName("document_front")
    DOCUMENT_FRONT,

    @SerializedName("document_back")
    DOCUMENT_BACK,

    @SerializedName("selfie")
    SELFIE,

    @SerializedName("video")
    VIDEO;

    internal companion object {
        fun MutableList<RequirementType>?.nextDestination(
            navActions: IdentityVerificationNavActions,
            verification: Verification
        ) {
            when {
                isNullOrEmpty() -> {
                    navActions.navigateToWelcome()
                }

                contains(CONSENT) -> {
                    navActions.navigateToWelcome()
                }

                intersect(listOf(COUNTRY, DOCUMENT_TYPE)).isNotEmpty() -> {
                    navActions.navigateToDocumentSelection()
                }

                intersect(listOf(DOCUMENT_FRONT, DOCUMENT_BACK)).isNotEmpty() -> {
                    navActions.navigateToDocumentCaptureMethods(verification.options.document.allowed.first())
                }

                contains(SELFIE) -> {
                    navActions.navigateToSelfie()
                }
            }
        }
    }
}