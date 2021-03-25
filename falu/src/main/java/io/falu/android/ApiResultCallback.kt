package io.falu.android

/**
 * Generic interface for an API operation callback that either returns a
 * result, [TResult], or an [Exception]
 */
interface ApiResultCallback<in TResult> {
    fun onSuccess(result: TResult)
    fun onError(e: Exception)
}