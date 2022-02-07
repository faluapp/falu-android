package io.falu.android


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.falu.android.models.evaluations.EvaluationRequest
import io.falu.android.models.payments.MpesaPaymentRequest
import io.falu.android.models.payments.PaymentRequest
import io.falu.android.networking.FaluApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "AndroidManifest.xml")
@ExperimentalCoroutinesApi
class FaluEndToEndTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val apiClient = FaluApiClient(context, FakeKeys.TEST_PUBLISHABLE_KEY, true)
    private val falu = Falu(context, FakeKeys.TEST_PUBLISHABLE_KEY, true)
    private val testDispatcher = TestCoroutineDispatcher()

    @AfterTest
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testCreateEvaluationThrowsException() {
        val request = EvaluationRequest(
            scope = "personal",
            name = "JOHN DOE",
            phone = "+2547123456789",
            password = "12345678",
            file = "file_602a8dd0a54847479a874de4",
        )

        runBlocking(Dispatchers.IO) {
            val response = apiClient.createEvaluation(request)
            assertEquals(true, !response.successful())
        }
    }

    @Test
    fun testMpesaPaymentInitRequest() {
        val mpesa = MpesaPaymentRequest(
            phone = "+254712345678",
            reference = "254712345678",
            paybill = true,
            destination = "00110"
        )

        val request = PaymentRequest(
            amount = 100,
            currency = "kes",
            mpesa = mpesa
        )

        runBlocking(Dispatchers.IO) {
            val response = apiClient.createPayment(request)
            assertEquals(true, response.successful())
        }
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }
}