package io.falu.android.networking

import android.content.Context
import io.falu.android.models.payments.Payment
import io.falu.android.models.payments.PaymentRequest
import io.falu.core.ApiResultCallback
import io.falu.core.models.FaluFile
import io.falu.core.models.FaluFileUploadArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Makes network requests to the Falu API.
 */
internal class FaluRepository internal constructor(
    context: Context,
    publishableKey: String,
    enableLogging: Boolean
) :
    BaseApiRepository(context, publishableKey, enableLogging) {

    /**
     * Create a payment asynchronously
     *
     * See [Create a payment](https://api.falu.io/v1/payments).
     * `POST /v1/payments`
     *
     * @param request [The payment request object](https://falu.io)
     * @param callbacks [ApiResultCallback] to receive the result or error
     *
     */
    fun createPaymentAsync(
        request: PaymentRequest,
        callbacks: ApiResultCallback<Payment>
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                faluApiClient.createPayment(request)
            }.fold(
                onSuccess = {
                    handleFaluResponse(it, callbacks)
                },
                onFailure = {
                    dispatchError(it, callbacks)
                }
            )
        }
    }

    /**
     * Upload a file asynchronously
     *
     * See [Upload a file](https://api.falu.io/v1/file).
     * `POST /v1/files`
     *
     * @param request [The upload request object](https://falu.io)
     * @param callbacks [ApiResultCallback] to receive the result or error
     *
     */
    fun uploadFileAsync(
        request: FaluFileUploadArgs,
        callbacks: ApiResultCallback<FaluFile>
    ) {
        launch(Dispatchers.IO) {
            runCatching {
                faluApiClient.uploadFile(request)
            }.fold(
                onSuccess = {
                    handleFaluResponse(it, callbacks)
                },
                onFailure = {
                    dispatchError(it, callbacks)
                }
            )
        }
    }
}