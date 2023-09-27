package io.falu.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RestrictTo
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
            .header("X-App-Version-Name", appVersionName)
            .header("X-App-Version-Code", appVersionCode)
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
                "$packageName/$appVersionName"
    }

    private val appVersionName: String
        get() {
            with(context.packageManager) {
                return try {
                    @Suppress("DEPRECATION")
                    getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
                } catch (e: PackageManager.NameNotFoundException) {
                    "nameNotFound"
                }
            }
        }

    private val appVersionCode: String
        get() {
            with(context.packageManager) {
                return try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        getPackageInfo(context.packageName, 0).longVersionCode.toString()
                    } else {
                        @Suppress("DEPRECATION")
                        getPackageInfo(context.packageName, 0).versionCode.toString()
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    "versionCodeNotFound"
                }
            }
        }

    private val packageName: String
        get() = context.packageName
}