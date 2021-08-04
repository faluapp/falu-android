package io.falu.android.models.payments

import java.util.*

/**
 * [The Money object](https://falu.io)
 */
internal data class Money(
    var amount: Number,
    var currency: Currency,
) {
    val toCents: Int
        get() {
            return when (currency.currencyCode) {
                else -> {
                    DEFAULT_CENTS.times(amount.toInt())
                }
            }
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