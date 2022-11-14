package io.falu.android


import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.falu.android.models.payments.MpesaPaymentRequest
import io.falu.android.models.payments.Payment
import io.falu.android.models.payments.PaymentRequest
import io.falu.android.networking.FaluRepository
import io.falu.core.ApiResultCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
class FaluEndToEndTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testDispatcher = TestCoroutineDispatcher()

    private val faluRepository = mock<FaluRepository>()

    private val falu = Falu(faluRepository)

    private val mockPaymentResultCallback = mock<ApiResultCallback<Payment>>()

    private val payment = Payment(
        id = "pa_123",
        amount = 10000,
        currency = "kes",
        status = "pending",
        updated = Date(),
        succeeded = null,
        authorizationId = "",
        type = "",
        mpesa = null,
        failure = null,
        reversalId = null,
        live = false
    )

    @Test
    fun `test mpesa payment works`() {

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

        falu.createPayment(request, mockPaymentResultCallback)

    }
}