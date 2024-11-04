package io.falu.identity.api.models.requirements

import com.google.gson.annotations.SerializedName
import io.falu.identity.api.models.verification.Verification
import io.falu.identity.navigation.DocumentCaptureDestination
import io.falu.identity.navigation.DocumentSelectionDestination
import io.falu.identity.navigation.IdentityVerificationNavActions
import io.falu.identity.navigation.SelfieDestination
import io.falu.identity.navigation.WelcomeDestination

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

        fun RequirementType.matchesFromRoute(fromRoute: String) =
            when (this) {
                CONSENT -> {
                    fromRoute == WelcomeDestination.ROUTE.route
                }

                COUNTRY, DOCUMENT_TYPE -> {
                    fromRoute == DocumentSelectionDestination.ROUTE.route
                }

                DOCUMENT_FRONT,
                DOCUMENT_BACK -> {
                    fromRoute == DocumentCaptureDestination.ROUTE.route
                }

                SELFIE -> {
                    fromRoute == SelfieDestination.ROUTE.route
                }

                VIDEO -> {
                    false
                }
            }

    }
}