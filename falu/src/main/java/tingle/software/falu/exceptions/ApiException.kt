package tingle.software.falu.exceptions

import software.tingle.api.HttpApiResponseProblem

/**
 * An [Exception] that represents an internal problem with Falu's servers.
 */
class APIException(
    problem: HttpApiResponseProblem? = null,
    statusCode: Int? = 0,
    errorCode: String? = problem?.code,
    message: String? = problem?.code,
    cause: Throwable? = null
) : FaluException(
    problem = problem,
    statusCode = statusCode,
    errorCode = errorCode,
    cause = cause,
    message = message
) {
    internal constructor(throwable: Throwable) : this(
        message = throwable.message,
        cause = throwable
    )
}