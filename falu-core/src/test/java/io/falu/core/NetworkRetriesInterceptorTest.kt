package io.falu.core

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(RobolectricTestRunner::class)
class NetworkRetriesInterceptorTest {
    private lateinit var retriesHeaderProvider: NetworkRetriesInterceptor
    private lateinit var mockChain: Interceptor.Chain
    private lateinit var mockRequest: Request

    @BeforeTest
    fun setUp() {
        retriesHeaderProvider = NetworkRetriesInterceptor(3) // Set max retries to 3
        mockChain = Mockito.mock(Interceptor.Chain::class.java)
        mockRequest = Mockito.mock(Request::class.java)

        `when`(mockChain.request()).thenReturn(mockRequest)
    }

    @Test
    fun test_NoRetry() {
        val mockResponse = Mockito.mock(Response::class.java)
        `when`(mockResponse.header("X-Should-Retry")).thenReturn("false")
        `when`(mockResponse.code).thenReturn(200) // No need to retry
        `when`(mockChain.proceed(mockRequest)).thenReturn(mockResponse)

        val response = retriesHeaderProvider.intercept(mockChain)
        assertEquals(mockResponse, response)
        verify(mockChain, times(1)).proceed(mockRequest)
    }

    @Test
    fun test_RetryWithHeader() {
        val mockResponse = Mockito.mock(Response::class.java)
        `when`(mockResponse.header("X-Should-Retry")).thenReturn("true")
        `when`(mockChain.proceed(mockRequest)).thenReturn(mockResponse)

        val response = retriesHeaderProvider.intercept(mockChain)
        assertEquals(mockResponse, response)
        verify(mockChain, times(4)).proceed(mockRequest) // 1 initial + 3 retries
    }

    @Test
    fun test_RetryOn409Conflict() {
        val mockResponse = Mockito.mock(Response::class.java)
        `when`(mockResponse.header("X-Should-Retry")).thenReturn("false")
        `when`(mockResponse.code).thenReturn(409) // Conflict
        `when`(mockChain.proceed(mockRequest)).thenReturn(mockResponse)

        val response = retriesHeaderProvider.intercept(mockChain)
        assertEquals(mockResponse, response)
        verify(mockChain, times(4)).proceed(mockRequest) // 1 initial + 3 retries
    }

    @Test
    fun test_RetryOn408Timeout() {
        val mockResponse = Mockito.mock(Response::class.java)
        `when`(mockResponse.header("X-Should-Retry")).thenReturn("false")
        `when`(mockResponse.code).thenReturn(408) // Request timeout
        `when`(mockChain.proceed(mockRequest)).thenReturn(mockResponse)

        val response = retriesHeaderProvider.intercept(mockChain)
        assertEquals(mockResponse, response)
        verify(mockChain, times(4)).proceed(mockRequest) // 1 initial + 3 retries
    }

    @Test
    fun test_MaxRetriesExceeded() {
        val mockResponse = Mockito.mock(Response::class.java)
        `when`(mockResponse.header("X-Should-Retry")).thenReturn("true")
        `when`(mockChain.proceed(mockRequest)).thenReturn(mockResponse)

        retriesHeaderProvider = NetworkRetriesInterceptor(2) // Max 2 retries
        val response = retriesHeaderProvider.intercept(mockChain)
        assertEquals(mockResponse, response)
        verify(mockChain, times(3)).proceed(mockRequest) // 1 initial + 2 retries
    }

    @Test
    fun test_RetryWhenHeaderIsNull() {
        val mockResponse = Mockito.mock(Response::class.java)
        `when`(mockResponse.header("X-Should-Retry")).thenReturn(null)
        `when`(mockResponse.code).thenReturn(503) // Service Unavailable, should trigger retry
        `when`(mockChain.proceed(mockRequest)).thenReturn(mockResponse)

        val response = retriesHeaderProvider.intercept(mockChain)
        assertEquals(mockResponse, response)
        verify(mockChain, times(4)).proceed(mockRequest) // 1 initial + 3 retries
    }
}