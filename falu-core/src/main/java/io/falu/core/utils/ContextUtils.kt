package io.falu.core.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ContextUtils {
    private val Context.packageInfo: PackageInfo?
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
        }

    val Context.appVersionCode: String
        get() {
            with(packageInfo) {
                return try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        this?.longVersionCode?.toString().orEmpty()
                    } else {
                        @Suppress("DEPRECATION")
                        this?.versionCode?.toString().orEmpty()
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    "versionCodeNotFound"
                }
            }
        }

    val Context.appName: CharSequence
        get() = packageInfo?.applicationInfo?.loadLabel(packageManager).takeUnless {
            it.isNullOrBlank()
        } ?: packageName

    val Context.appVersionName: String
        get() {
            with(packageInfo) {
                return try {
                    this?.versionName ?: "0.0.0"
                } catch (e: PackageManager.NameNotFoundException) {
                    "nameNotFound"
                }
            }
        }
}