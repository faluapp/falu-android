package tingle.software.falu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tingle.software.falu.model.EvaluationRequest
import tingle.software.falu.model.EvaluationResponse
import tingle.software.falu.model.EvaluationScope
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val falu = Falu("pk_test_cbw2Bxslzkxf7sUAg6932NYm1ApTX7C0TEMbvCYss")

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


        falu.createEvaluation(request, object : ApiResultCallback<EvaluationResponse>{
            override fun onSuccess(result: EvaluationResponse) {
                TODO("Not yet implemented")
            }

            override fun onError(e: Exception) {
                print(e)
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