package io.falu.identity.models

import com.google.gson.annotations.SerializedName

data class IdentityVerificationCreationRequest(
    /**
     * The type of verification check to be performed.The type of verification check to be performed.
     */
    var type: String,
    /**
     * The URL the user will be redirected to upon completing the verification flow.
     */
    @SerializedName("return_url")
    var returnUrl: String? = null,

    /**
     * A set of verification checks to be performed.
     */
    var options: IdentityVerificationOptions?
)