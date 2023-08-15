package io.falu.android

import io.falu.core.ApiKeyValidator
import kotlin.test.assertFailsWith
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiKeyValidatorTests {

    @Test
    fun testPublishableKey() {
        assertEquals(
            FakeKeys.FAKE_PUBLISHABLE_KEY,
            ApiKeyValidator.get().requireValid(FakeKeys.FAKE_PUBLISHABLE_KEY)
        )
    }

    @Test
    fun testSecretKeyThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            ApiKeyValidator.get().requireValid(FakeKeys.FAKE_SECRET_KEY)
        }
    }

    @Test
    fun testEmptyKeyThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            ApiKeyValidator.get().requireValid(" ")
        }
    }

    @Test
    fun testNullThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            ApiKeyValidator.get().requireValid(null)
        }
    }
}