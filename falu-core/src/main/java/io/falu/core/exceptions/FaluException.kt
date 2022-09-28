package io.falu.core.exceptions

import androidx.annotation.RestrictTo
import org.json.JSONException
import software.tingle.api.HttpApiResponseProblem
import java.io.IOException
import java.util.*

/**
 * A base class for Falu-related exceptions.
 */
abstract class FaluException(
    val problem: HttpApiResponseProblem? = null,
    val statusCode: Int? = 0,
    val errorCode: String? = problem?.code,
    cause: Throwable? = null,
    message: String? = problem?.description
) : Exception(message, cause) {


    override fun hashCode(): Int {
        return Objects.hash(problem, statusCode, errorCode, message)
    }

    private fun typedEquals(ex: FaluException): Boolean {
        return problem == ex.problem &&
                statusCode == ex.statusCode &&
                errorCode == ex.errorCode &&
                message == ex.message
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other is FaluException -> typedEquals(other)
            else -> false
        }
    }

    override fun toString(): String {
        return listOfNotNull(
            statusCode.let { "Response code: $it" },
            errorCode.let { "Error Code: $it" },
            message.let { "$it" },
            super.toString()
        ).joinToString(separator = "\n")
    }

    companion object {
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun create(throwable: Throwable): FaluException {
            return when (throwable) {
                is FaluException -> throwable
                is JSONException -> APIException(throwable)
                is IOException -> APIConnectionException.create(throwable)
                else -> APIException(throwable)
            }
        }
    }
}