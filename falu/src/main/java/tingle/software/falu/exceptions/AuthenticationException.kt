package tingle.software.falu.exceptions

import software.tingle.api.HttpApiResponseProblem
import java.net.HttpURLConnection

/**
 * No valid API key provided.
 *
 * [Errors](https://stripe.com/docs/api/errors)
 */
class AuthenticationException internal constructor(
    problem: HttpApiResponseProblem,
) : FaluException(
    problem,
    HttpURLConnection.HTTP_UNAUTHORIZED
)