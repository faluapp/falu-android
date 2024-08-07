package io.falu.identity.models

import com.google.gson.annotations.SerializedName

data class IdentityVerificationOptions(
    /**
     * Whether to allow uploads for documents, images and videos. This only applies to document related checks.
     */
    @SerializedName("allow_uploads")
    var allowUploads: Boolean? = null,

    /**
     * Options for the id number check.
     */
    @SerializedName("id_number")
    var idNumber: IdentityVerificationOptionsForIdNumber? = null,

    /**
     * Options for the document check.
     */
    var document: IdentityVerificationOptionsForDocument? = null,

    /**
     * Options for the selfie check.
     */
    var selfie: IdentityVerificationOptionsForSelfie? = null,

    /**
     * Options for the video check.
     */
    var video: IdentityVerificationOptionsForVideo? = null,

    /**
     * Options for the tax pin check.
     */
    @SerializedName("tax_id")
    var tax: IdentityVerificationOptionsForTax? = null
)