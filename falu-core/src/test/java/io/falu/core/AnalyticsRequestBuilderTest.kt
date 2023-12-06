package io.falu.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AnalyticsRequestBuilderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val factory = AnalyticsRequestBuilder(context = context, client = CLIENT)

    @Test
    fun `verify client exists`() {
        val request = factory.createRequest(event = "EVENT_TEST")
        assertEquals(request.client, CLIENT)
    }

    @Test
    fun `verify sdk properties populated`() {
        val request = factory.createRequest(
            event = "EVENT_TEST",
            mapOf("key_1" to "key one", "key_2" to "key two", "key_3" to "key three")
        )
        assertEquals("native", request.plugin.type)
        assertEquals("android", request.sdk.platform)
        assertTrue(request.metadata.isNotEmpty())
    }

    companion object {
        private const val CLIENT = "test_client"
    }
}