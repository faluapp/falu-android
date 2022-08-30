package io.falu.android.models.identityVerification

data class IdentityVerificationOptionsForSelfie(
    /**
     * Disable image uploads, selfie images have to be captured using the device's camera.
     */
    var live: Boolean?
)
