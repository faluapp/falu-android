package io.falu.identity.models

data class IdentityVerificationOptionsForDocument(
    /**
     * The allowed identity document types. If a user uploads a document which isn't one of the allowed types, it will be rejected.
     */
    var allowed: MutableList<String>?
)