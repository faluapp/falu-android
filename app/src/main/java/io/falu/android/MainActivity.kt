package io.falu.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.falu.android.model.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val falu =
            Falu(this, "pk_test_cbw2Bxslzkxf7sUAg6932NYm1ApTX7C0TEMbvCYss", BuildConfig.DEBUG)

        val file = File(cacheDir, "falu.pdf")
        val fileStream = resources.openRawResource(R.raw.falu)
        copyStreamToFile(fileStream, file)

        val request = EvaluationRequest(
            scope = EvaluationScope.PERSONAL,
            name = "JOHN DOE",
            phone = "+2547123456789",
            password = "12345678",
            file = file,
            description = ""
        )


        falu.createEvaluation(request, object : ApiResultCallback<EvaluationResponse> {
            override fun onSuccess(result: EvaluationResponse) {
                print(result.id)
            }

            override fun onError(e: Exception) {
                print(e)
            }
        })

        val mpesa = PaymentInitiationMpesa()
        mpesa.phone = "+254712345678"
        mpesa.reference = "+254712345678"
        mpesa.kind = MpesaStkPushTransactionType.CUSTOMER_PAYS_BILL_ONLINE

        val paymentRequest = PaymentRequest(
            amount = 100,
            currency = "kes",
            mpesa = mpesa
        )

        falu.createPayment(paymentRequest, object : ApiResultCallback<Payment> {
            override fun onSuccess(result: Payment) {

            }

            override fun onError(e: Exception) {
            }
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