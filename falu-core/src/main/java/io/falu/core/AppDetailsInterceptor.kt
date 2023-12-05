package io.falu.core

import android.content.Context
import android.os.Build
import androidx.annotation.RestrictTo
import io.falu.core.utils.ContextUtils.appVersionCode
import io.falu.core.utils.ContextUtils.appVersionName
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An @[Interceptor] that adds headers for package id, version name and version code to a request before sending
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AppDetailsInterceptor(private val context: Context) : Interceptor {

    private val userAgent: String by lazy {
        buildUserAgent()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("X-App-Package-Id", packageName)
            .header("X-App-Version-Name", context.appVersionName)
            .header("X-App-Version-Code", context.appVersionCode)
            .header("User-Agent", userAgent)
            .header("X-App-Kind", "android")
            .build()

        return chain.proceed(request)
    }

    private fun buildUserAgent(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val versionRelease = Build.VERSION.RELEASE

        return "falu-android/${BuildConfig.FALU_VERSION_NAME} (Android $versionRelease; $manufacturer $model) " +
                "$packageName/${context.appVersionName}"
    }

    private val packageName: String
        get() = context.packageName
}