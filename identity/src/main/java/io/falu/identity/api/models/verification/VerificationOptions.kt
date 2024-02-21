package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName

internal data class VerificationOptions(
    /**
     * Supported document issuing countries
     */
    var countries: MutableList<String>,

    /**
     * Whether to allow uploads for documents, images and videos. This only applies to document related checks.
     */
    @SerializedName("allow_uploads")
    var allowUploads: Boolean = false,

    /**
     * The maximum number of verification attempts the user is allowed to make.
     * A large number of attempts could signal fraudulent behavior.
     * */
    @SerializedName("max_attempts")
    var maxAttempts: Int? = null,

    /**
     * Options for the document check.
     */
    var document: VerificationOptionsForDocument,

    /**
     * Options for the selfie check.
     */
    var selfie: VerificationOptionsForSelfie? = null,

    /**
     * Options for the video check.
     */
    var video: VerificationOptionsForVideo? = null
)