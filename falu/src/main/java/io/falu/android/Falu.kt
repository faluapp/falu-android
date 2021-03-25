package io.falu.android

import io.falu.android.model.EvaluationRequest
import io.falu.android.model.EvaluationResponse
import io.falu.android.networking.FaluRepository

/**
 * Entry-point to the Falu SDK
 *
 * Supports asynchronous methods to access the following Falu APIs:
 *
 * * [Evaluations API] [Create Evaluation] - Create an evaluation
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
    constructor(
        publishableKey: String, enableLogging: Boolean = false
    ) : this(
        ApiKeyValidator.get().requireValid(publishableKey),
        FaluRepository(publishableKey, enableLogging)
    )


    /**
     * Create an evaluation asynchronously
     *
     * See [Create an evaluation](https://api.falu.io/v1/evaluations).
     * `POST /v1/evaluations`
     *
     * @param request [The evaluation request object](https://falu.io)
     * @param callbacks [ApiResultCallback] to receive the result or error
     *
     */
    fun createEvaluation(
        request: EvaluationRequest,
        callbacks: ApiResultCallback<EvaluationResponse>
    ) {
        faluRepository.createEvaluationAsync(request, callbacks)
    }
}