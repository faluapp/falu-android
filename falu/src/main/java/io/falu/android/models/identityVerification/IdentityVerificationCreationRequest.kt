package io.falu.android.models.identityVerification

import com.google.gson.annotations.SerializedName
import io.falu.android.models.FaluModel

data class IdentityVerificationCreationRequest(
    /**
     * The type of verification check to be performed.The type of verification check to be performed.
     */
    var type: String,
    /**
     * The URL the user will be redirected to upon completing the verification flow.
     */
    @SerializedName("return_url")
    var returnUrl: String,

    /**
     * A set of verification checks to be performed.
     */
    var options: IdentityVerificationOptions?,
) : FaluModel()