package io.falu.android.networking

import android.content.Context
import io.falu.core.exceptions.ApiException
import io.falu.core.exceptions.FaluException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import software.tingle.api.ResourceResponse

/**
 * A base class for Falu-related API requests.
 */
internal abstract class BaseApiRepository internal constructor(
    context: Context,
    publishableKey: String,
    maxNetworkRetries: Int = 0,
    enableLogging: Boolean
) :
    CoroutineScope {
    protected val faluApiClient = FaluApiClient(
        context,
        publishableKey,
        maxNetworkRetries,
        enableLogging
    )

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.Main

    // handle falu Resource Response
    protected suspend fun <TResource> handleFaluResponse(
        response: ResourceResponse<TResource>?,
        callbacks: io.falu.core.ApiResultCallback<TResource>
    ) = withContext(Dispatchers.Main) {
        if (response != null && response.successful() && response.resource != null) {
            callbacks.onSuccess(response.resource!!)
            return@withContext
        }

        val exception = ApiException(problem = response?.error, statusCode = response?.statusCode)
        dispatchError(exception, callbacks)
    }

    // dispatch errors
    protected suspend fun dispatchError(
        throwable: Throwable,
        callback: io.falu.core.ApiResultCallback<*>
    ) = withContext(Dispatchers.Main) {
        callback.onError(FaluException.create(throwable))
    }
}