package io.falu.android

import io.falu.android.models.payments.Money
import org.junit.Test
import java.util.Currency
import kotlin.test.assertEquals

class MoneyPatternTests {

    @Test
    fun testAmountConversions() {
        val money = Money(
            amount = 20000,
            currency = Currency.getInstance("kes".uppercase())
        )
        assertEquals(2000000, money.amountInMinorUnits)
    }
}