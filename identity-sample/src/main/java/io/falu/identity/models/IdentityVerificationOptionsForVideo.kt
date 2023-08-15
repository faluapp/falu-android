package io.falu.identity.models

data class IdentityVerificationOptionsForVideo(
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