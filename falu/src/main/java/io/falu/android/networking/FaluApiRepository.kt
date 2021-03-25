package io.falu.android.networking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import software.tingle.api.ResourceResponse
import io.falu.android.ApiResultCallback
import io.falu.android.exceptions.APIException
import io.falu.android.exceptions.FaluException
import kotlin.coroutines.CoroutineContext

/**
 * A base class for Falu-related API requests.
 */
internal abstract class FaluApiRepository internal constructor(
    publishableKey: String,
    enableLogging: Boolean = false
) :
    CoroutineScope {
    protected val faluApiClient = FaluApiClient(publishableKey, enableLogging)

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.Main


    // handle falu Resource Response
    protected suspend fun <TResource> handleFaluResponse(
        response: ResourceResponse<TResource>?,
        callbacks: ApiResultCallback<TResource>
    ) = withContext(Dispatchers.Main) {
        if (response != null && response.successful() && response.resource != null) {
            callbacks.onSuccess(response.resource!!)
            return@withContext
        }

        val exception = APIException(problem = response?.error, statusCode = response?.statusCode)
        dispatchError(exception, callbacks)
    }

    // dispatch errors
    protected suspend fun dispatchError(
        throwable: Throwable,
        callback: ApiResultCallback<*>
    ) = withContext(Dispatchers.Main) {
        callback.onError(FaluException.create(throwable))
    }

}