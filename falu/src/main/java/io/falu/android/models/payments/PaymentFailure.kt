package io.falu.android.models.payments

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.Parcelize

/**
 * [The Payment Failure object](https://falu.io)
 */
@Parcelize
data class PaymentFailure(
    /**
     * Reason for failure of a payment, transfer or reversal
     */
    var reason: String,

    /**
     * Time at which failure occurred
     */
    var timestamp: Date?,

    /**
     * Failure message as received from the provider
     */
    var detail: String?
) : Parcelable