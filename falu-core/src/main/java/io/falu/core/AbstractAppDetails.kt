package io.falu.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RestrictTo
import okhttp3.Interceptor
import okhttp3.Response

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class AbstractAppDetails(private val context: Context) : Interceptor {

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
            .header("User-Agent", userAgent)
            .build()

        return chain.proceed(request)
    }

    abstract fun buildUserAgent(context: Context): String

    private val appVersionName: String
        get() {
            with(context.packageManager) {
                return try {
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