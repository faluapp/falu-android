package tingle.software.falu.exceptions

import software.tingle.api.HttpApiResponseProblem

/**
 * An [Exception] that represents an internal problem with Falu's servers.
 */
class APIException(
    problem: HttpApiResponseProblem? = null,
    statusCode: Int? = 0,
    message: String? = problem?.description,
    cause: Throwable? = null
) : FaluException(
    problem = problem,
    statusCode = statusCode,
    cause = cause,
    message = message
) {
    internal constructor(throwable: Throwable) : this(
        message = throwable.message,
        cause = throwable
    )
}