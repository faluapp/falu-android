package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName
import io.falu.identity.api.models.VerificationOptionsForDocument
import io.falu.identity.api.models.VerificationOptionsForSelfie
import io.falu.identity.api.models.VerificationOptionsForVideo

internal data class VerificationOptions(
    /**
     * Supported document issuing countries
     */
    var countries: MutableList<String>,

    /**
     * Whether to allow uploads for documents, images and videos. This only applies to document related checks.
     */
    @SerializedName("allow_uploads")
    var allowUploads: Boolean? = null,

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