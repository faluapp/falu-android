package io.falu.android.exceptions

import software.tingle.api.HttpApiResponseProblem
import java.net.HttpURLConnection

/**
 * No valid API key provided.
 *
 */
class AuthenticationException internal constructor(
    problem: HttpApiResponseProblem,
) : FaluException(
    problem,
    HttpURLConnection.HTTP_UNAUTHORIZED
)