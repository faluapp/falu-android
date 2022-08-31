package io.falu.identity.models

data class IdentityVerificationOptionsForVideo(
    /**
     * Disable uploads, videos have to be captured using the device's camera.
     */
    var live: Boolean?,

    /**
     * Face poses to be performed in the video recording.
     * It is recommended to leave this field unassigned for the server to
     * generate random values per verification for security purposes.
     */
    var poses: MutableList<String>?,

    /**
     * Numerical phrase to be recited in the video recording.
     * When not provided, the server generates a random one.
     */
    var recital: Int?
)
