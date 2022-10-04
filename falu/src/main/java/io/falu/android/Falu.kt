package io.falu.android

import android.content.Context
import io.falu.core.models.FaluFile
import io.falu.android.models.files.UploadRequest
import io.falu.android.models.payments.Payment
import io.falu.android.models.payments.PaymentRequest
import io.falu.android.networking.FaluRepository
import io.falu.core.ApiKeyValidator
import io.falu.core.ApiResultCallback

/**
 * Entry-point to the Falu SDK
 *
 * Supports asynchronous methods to access the following Falu APIs:
 *
 */
class Falu internal constructor(
    publishableKey: String,
    private val faluRepository: FaluRepository
) {
    private val publishableKey = ApiKeyValidator().requireValid(publishableKey)

    /**
     * Constructor with publishable key.
     *
     * @param publishableKey the client's publishable key
     * @param enableLogging enable logging in FALU SDK; disabled by default.
     * It is recommended to disable logging in production.
     */
    @JvmOverloads
    constructor(
        context: Context,
        publishableKey: String,
        enableLogging: Boolean = false
    ) : this(
        ApiKeyValidator.get().requireValid(publishableKey),
        FaluRepository(context, publishableKey, enableLogging)
    )


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
    fun createPayment(
        request: PaymentRequest,
        callbacks: ApiResultCallback<Payment>
    ) {
        faluRepository.createPaymentAsync(request, callbacks)
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
    fun createFile(request: UploadRequest, callbacks: ApiResultCallback<FaluFile>) {
        faluRepository.uploadFileAsync(request, callbacks)
    }
}