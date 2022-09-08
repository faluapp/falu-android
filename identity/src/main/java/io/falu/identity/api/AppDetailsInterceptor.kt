package io.falu.identity.api

import android.content.Context
import android.os.Build
import androidx.annotation.RestrictTo
import io.falu.core.AbstractAppDetails

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class AppDetailsInterceptor internal constructor(context: Context) :
    AbstractAppDetails(context) {
    override fun buildUserAgent(context: Context): String {
        with(context.packageManager) {

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            val installerName = getInstallerPackageName(context.packageName) ?: "StandAloneInstall"

            return "falu-identity/${1.0}(${100}); $installerName; ($manufacturer; $model; SDK $version; Android $versionRelease)"
        }
    }
}