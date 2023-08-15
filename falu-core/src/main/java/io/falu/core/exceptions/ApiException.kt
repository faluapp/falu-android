package io.falu.core.exceptions

import androidx.annotation.RestrictTo
import software.tingle.api.HttpApiResponseProblem

/**
 * An [Exception] that represents an internal problem with Falu's servers.
 */
class ApiException(
    problem: HttpApiResponseProblem? = null,
    statusCode: Int? = 0,
    errorCode: String? = problem?.code,
    message: String? = problem?.description,
    cause: Throwable? = null
) : FaluException(
    problem = problem,
    statusCode = statusCode,
    errorCode = errorCode,
    cause = cause,
    message = message
) {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    constructor(throwable: Throwable) : this(
        message = throwable.message,
        cause = throwable
    )
}