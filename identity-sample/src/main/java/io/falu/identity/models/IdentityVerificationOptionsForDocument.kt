package io.falu.identity.models

data class IdentityVerificationOptionsForDocument(
    /**
     * Disable image uploads, identity document images have to be captured using the device's camera.
     */
    var live: Boolean? = null,

    /**
     * The allowed identity document types. If a user uploads a document which isn't one of the allowed types, it will be rejected.
     */
    var allowed: MutableList<String>?
)
