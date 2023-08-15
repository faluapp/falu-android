package io.falu.core.exceptions

import androidx.annotation.RestrictTo
import software.tingle.api.HttpApiResponseProblem
import java.net.HttpURLConnection

/**
 * No valid API key provided.
 */
class AuthenticationException
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) constructor(problem: HttpApiResponseProblem) :
    FaluException(
        problem,
        HttpURLConnection.HTTP_UNAUTHORIZED
    )