package io.falu.identity.api.models.verification

import com.google.gson.annotations.SerializedName

internal data class VerificationOptionsForVideo(
    /**
     * Face poses to be performed in the video recording.
     * It is recommended to leave this field unassigned for the server to
     * generate random values per verification for security purposes.
     */
    var poses: MutableList<VerificationVideoPose>?,

    /**
     * Numerical phrase to be recited in the video recording.
     * When not provided, the server generates a random one.
     */
    var recital: Int?
)

internal enum class VerificationVideoPose {
    @SerializedName("blink")
    BLINK,

    @SerializedName("turn_left")
    TURN_LEFT,

    @SerializedName("turn_right")
    TURN_RIGHT,

    @SerializedName("nod")
    NOD,

    @SerializedName("smile")
    SMILE
}