package io.falu.identity.api.models.verification

import io.falu.identity.api.models.IdentityDocumentType

internal data class VerificationOptionsForDocument(
    /**
     * The allowed identity document types. If a user uploads a document which isn't one of the allowed types, it will be rejected.
     */
    var allowed: MutableList<IdentityDocumentType>
)
