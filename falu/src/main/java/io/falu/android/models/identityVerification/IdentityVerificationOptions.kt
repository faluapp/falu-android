package io.falu.android.models.identityVerification

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
    var idNumber: IdentityVerificationOptionsForIdNumber?,

    /**
     * Options for the document check.
     */
    var document: IdentityVerificationOptionsForDocument?,

    /**
     * Options for the selfie check.
     */
    var selfie: IdentityVerificationOptionsForSelfie?,

    /**
     * Options for the video check.
     */
    var video: IdentityVerificationOptionsForVideo?
)