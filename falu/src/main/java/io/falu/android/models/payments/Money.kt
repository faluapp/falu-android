package io.falu.android.models.payments

import java.util.*
import kotlin.math.pow

/**
 * [The Money object](https://falu.io)
 */
internal data class Money(
    var amount: Number,
    var currency: Currency,
) {
    /// Minor Unit is a fraction of the base (ex. cents, stotinka, etc.)
    val amountInMinorUnits: Int
        get() {
            val units = 10.toDouble().pow(currency.defaultFractionDigits)
            return (amount.toDouble().times(units)).toInt()
        }

    private val toShillings: Int
        get() {
            return when (currency.currencyCode) {
                else -> {
                    DEFAULT_CENTS.times(amount.toInt())
                }
            }
        }

    val displayName: String
        get() {
            return "${currency.currencyCode} $toShillings"
        }

    internal companion object {
        private const val DEFAULT_CENTS = 100
    }
}