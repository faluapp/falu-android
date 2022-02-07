package io.falu.android.models.payments

import com.google.gson.annotations.JsonAdapter
import io.falu.android.models.FaluModel
import kotlinx.parcelize.Parcelize
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.*

/**
 * [The payment object](https://falu.io)
 */
@Parcelize
data class Payment(
    /**
     * Unique identifier for the object
     */
    var id: String,

    /**
     * Amount of the payment in smallest currency unit.
     */
    var amount: Int,

    /**
     * Three-letter ISO currency code, in lowercase
     */
    var currency: String,

    /**
     * The status of a payment
     */
    var status: String,

    /**
     * Time at which the object was created.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var created: Date = Date(),

    /**
     * Time at which the object was last updated
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var updated: Date?,

    /**
     * Time at which the payment succeeded.
     * Only populated when successful.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    var succeeded: Date?,

    /**
     * Identifier of the authorization, if the payment passed through a flow requiring authorization.
     */
    var authorizationId: String?,

    /**
     * The medium used for the payment
     */
    var type: String?,

    /**
     * Represents the provider details for a MPESA payment
     */
    var mpesa: Mpesa?,

    /**
     * Details about failure of a payment, transfer or reversal.
     */
    var failure: PaymentFailure?,

    /**
     * Identifier of the reversal, if the payment has been reversed
     */
    var reversalId: String?,

    /**
     * Unique identifier for the workspace that the object belongs to
     */
    var workspace: String?,

    /**
     * Indicates if this object belongs in the live environment
     */
    var live: Boolean
) : FaluModel()
