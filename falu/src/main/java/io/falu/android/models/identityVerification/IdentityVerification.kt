package io.falu.android.models.identityVerification

import com.google.gson.annotations.SerializedName
import io.falu.android.models.FaluModel

data class IdentityVerification(
    /**
     * Unique identifier for the record
     */
    var id: String,

    /**
     * Status of the verification.
     * <a href="https://falu.io/docs/identity/how-verifications-work">Learn more about the lifecycle of verifications.</a>
     */
    var status: String?,

    /**
     * The type of <a href="https://falu.io/docs/identity/verification-checks">verification check<a/> to be performed.
     */
    var type: String?,

    /**
     * A set of verification checks to be performed.
     */
    var options: IdentityVerificationOptions?,

    /**
     * The short-lived client secret used by front-end libraries to show a verification modal inside your app.
     * This client secret expires after 24 hours and can only be used once.
     * Don’t store it, log it, embed it in a URL, or expose it to anyone other than the user.
     * Make sure that you have TLS enabled on any page that includes the client secret.
     */
    @SerializedName("client_secret")
    var clientSecret: String?,

    /**
     * The short-lived URL that you use to redirect a user to Falu to submit their identity information.
     * This link expires after 24 hours and can only be used once.
     * Don’t store it, log it, send it in emails or expose it to anyone other than the target user.
     */
    var url: String?,

    /**
     * Unique identifiers of the reports for this verification.
     */
    var reports: MutableList<String>?

) : FaluModel()