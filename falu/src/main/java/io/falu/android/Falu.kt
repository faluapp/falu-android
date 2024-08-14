package io.falu.android

import android.content.Context
import io.falu.android.models.payments.Payment
import io.falu.android.models.payments.PaymentRequest
import io.falu.android.networking.FaluRepository
import io.falu.core.ApiKeyValidator
import io.falu.core.ApiResultCallback
import io.falu.core.models.FaluFile
import io.falu.core.models.FaluFileUploadArgs

/**
 * Entry-point to the Falu SDK
 *
 * Supports asynchronous methods to access the following Falu APIs:
 *
 */
class Falu internal constructor(
    private val faluRepository: FaluRepository
) {

    /**
     * Constructor with publishable key.
     *
     * @param publishableKey the client's publishable key.
     * @param maxNetworkRetries the maximum number of network retries.
     * @param enableLogging enable logging in FALU SDK; disabled by default.
     * It is recommended to disable logging in production.
     */
    @JvmOverloads
    constructor(
        context: Context,
        publishableKey: String,
        maxNetworkRetries: Int = 0,
        enableLogging: Boolean = false
    ) : this(
        FaluRepository(
            context,
            ApiKeyValidator.get().requireValid(publishableKey),
            maxNetworkRetries,
            enableLogging
        )
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
    fun createFile(request: FaluFileUploadArgs, callbacks: ApiResultCallback<FaluFile>) {
        faluRepository.uploadFileAsync(request, callbacks)
    }
}