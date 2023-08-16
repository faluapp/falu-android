package io.falu.identity.api.models

import com.google.gson.annotations.SerializedName

internal data class WorkspaceInfo(
    /**
     * The name of the workspace
     */
    var name: String,

    /**
     * The country
     */
    var country: String
)

internal data class BusinessInfo(
    /**
     * The name of the business
     */
    var name: String?,

    /**
     * The website of the business
     */
    var website: String?,

    /**
     * The privacy policy URL of the business
     */
    @SerializedName("privacy_policy_url")
    var privacyPolicyUrl: String?,

    /**
     * The Terms of Service URL of the business
     */
    @SerializedName("terms_of_service_url")
    var tosUrl: String?
)

internal data class Branding(
    @SerializedName("logo_url")
    var logoUrl: String?,

    @SerializedName("icon_url")
    var iconUrl: String?
)

internal data class Support(
    var email: String?,
    var phone: String?,
    var url: String?
)