package io.falu.android.model

import java.util.*

/**
 * [The Money object](https://falu.io)
 */
class Money(
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