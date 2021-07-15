package io.falu.android.networking

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

abstract class AbstractAppDetails(private val context: Context) {

    protected val appVersionName: String
        get() {
            with(context.packageManager) {
                return try {
                    getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
                } catch (e: PackageManager.NameNotFoundException) {
                    "nameNotFound"
                }
            }
        }

    protected val appVersionCode: String
        get() {
            with(context.packageManager) {
                return try {
                    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
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

    protected val packageName: String
        get() = context.packageName
}