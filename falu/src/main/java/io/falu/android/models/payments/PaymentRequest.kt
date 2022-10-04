package io.falu.android.models.payments

import io.falu.core.models.FaluModel
import java.util.*

/**
 * [The payment request object](https://falu.io)
 */
data class PaymentRequest(
    /**
     * Amount of the payment in smallest currency unit.
     */
    var amount: Int,
    /**
     * Three-letter ISO currency code, in lowercase.
     */
    var currency: String,
    /**
     * Represents the provider details for a MPESA payment
     */
    var mpesa: MpesaPaymentRequest? = null
) : FaluModel() {
    init {
        this.amount = Money(
            amount,
            Currency.getInstance(currency.uppercase())
        ).amountInMinorUnits
    }
}
