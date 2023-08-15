package io.falu.android.networking

import android.content.Context
import android.os.Build
import androidx.annotation.RestrictTo
import io.falu.android.BuildConfig
import io.falu.core.AbstractAppDetails
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An @[Interceptor] that adds headers for package id, version name and version code to a request before sending
 * @param appKind the type of app making the request, android or iOS. Default is android.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class AppDetailsInterceptor internal constructor(
    context: Context,
    private val appKind: String = "android"
) : AbstractAppDetails(context) {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("X-App-Kind", appKind)
            .build()

        return chain.proceed(request)
    }

    override fun buildUserAgent(context: Context): String {
        with(context.packageManager) {

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            val installerName = getInstallerPackageName(context.packageName) ?: "StandAloneInstall"

            return "falu-android/${BuildConfig.FALU_VERSION_NAME}" +
                    "(${BuildConfig.FALU_VERSION_CODE});" +
                    " $installerName; " +
                    "($manufacturer; $model; SDK $version; Android $versionRelease)"
        }
    }
}