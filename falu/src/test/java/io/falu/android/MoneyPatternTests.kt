package io.falu.android

import io.falu.android.models.payments.Money
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class MoneyPatternTests {

    @Test
    fun testAmountConversions() {
        val money = Money(
            amount = 20000,
            currency = Currency.getInstance("kes".toUpperCase())
        )
        assertEquals(2000000, money.toCents)
    }

}