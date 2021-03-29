package io.falu.android


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.argWhere
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.falu.android.exceptions.FaluException
import io.falu.android.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class FaluEndToEndTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val falu = Falu(FakeKeys.TEST_PUBLISHABLE_KEY)

    private val testDispatcher = TestCoroutineDispatcher()

    @AfterTest
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testCreateEvaluationThrowsException() {
        val createEvalCallback: ApiResultCallback<EvaluationResponse> = mock()

        val file = File(context.cacheDir, "falu.pdf")
        val fileStream = context.resources.openRawResource(R.raw.falu)
        copyStreamToFile(fileStream, file)

        val request = EvaluationRequest(
            scope = EvaluationScope.PERSONAL,
            name = "JOHN DOE",
            phone = "+2547123456789",
            password = "12345678",
            file = file,
            description = ""
        )

        falu.createEvaluation(request, createEvalCallback)

        verify(createEvalCallback).onError(
            argWhere {
                it is FaluException
            }
        )
    }

    @Test
    fun testMpesaPaymentInitRequest() {
        val callback: ApiResultCallback<Payment> = mock()

        val mpesa = PaymentInitiationMpesa()
        mpesa.phone = "+254712345678"
        mpesa.reference = "+254712345678"
        mpesa.kind = MpesaStkPushTransactionType.CUSTOMER_PAYS_BILL_ONLINE

        val request = PaymentRequest(
            amount = 100,
            currency = "kes",
            mpesa = mpesa
        )

        falu.createPayment(request, callback)

        verify(callback).onSuccess(result = argWhere {
            it.status != PaymentStatus.FAILED && it.mpesa != null
        })
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