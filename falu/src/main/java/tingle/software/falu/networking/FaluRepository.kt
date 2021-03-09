package tingle.software.falu.networking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tingle.software.falu.ApiResultCallback
import tingle.software.falu.model.EvaluationRequest
import tingle.software.falu.model.EvaluationResponse

/**
 * Makes network requests to the Falu API.
 */
internal class FaluRepository internal constructor(publishableKey: String, enableLogging: Boolean) :
    FaluApiRepository(publishableKey, enableLogging) {

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
    fun createEvaluationAsync(
        request: EvaluationRequest,
        callbacks: ApiResultCallback<EvaluationResponse>
    ) {
        launch(Dispatchers.IO) {
            kotlin.runCatching {
                faluApiClient.createEvaluation(request)
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