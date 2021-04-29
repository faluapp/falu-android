package io.falu.android.networking

import android.content.Context
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An @[Interceptor] that adds headers for package id, version name and version code to a request before sending
 * @param appKind the type of app making the request, android or iOS. Default is android.
 */

internal class AppDetailsInterceptor internal constructor(
    context: Context,
    private val appKind: String = "android"
) : AbstractAppDetails(context), Interceptor {

    private val userAgent: String by lazy {
        buildUserAgent(context)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain
            .request()
            .newBuilder()
            .header("X-App-Package-Id", packageName)
            .header("X-App-Version-Name", appVersionName)
            .header("X-App-Version-Code", appVersionCode)
            .header("Falu-Agent", userAgent)
            .build()

        return chain.proceed(request)
    }

    @Suppress("DEPRECATION")
    internal fun buildUserAgent(context: Context): String {
        with(context.packageManager) {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            val appName = if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
            else context.getString(stringId)

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            val installerName = getInstallerPackageName(context.packageName) ?: "StandAloneInstall"

            return "$appName / $appVersionName($appVersionCode); $installerName; ($manufacturer; $model; SDK $version; Android $versionRelease)"
        }
    }
}
