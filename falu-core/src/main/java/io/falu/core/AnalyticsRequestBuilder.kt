package io.falu.core

import android.content.Context
import android.os.Build
import androidx.annotation.RestrictTo
import io.falu.core.models.AnalyticsTelemetry
import io.falu.core.models.AppInfo
import io.falu.core.models.DeviceInfo
import io.falu.core.models.OSInfo
import io.falu.core.models.PluginInfo
import io.falu.core.models.SDKInfo
import io.falu.core.utils.ContextUtils.appName
import io.falu.core.utils.ContextUtils.appVersionCode

/**
 * Build analytics parameters
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class AnalyticsRequestBuilder(context: Context, private val client: String, private val origin: String) {
    private val appContext = context.applicationContext

    fun createRequest(event: String, params: Map<String, Any?> = mapOf()) = AnalyticsTelemetry(
        name = event,
        client = client,
        os = OSInfo(version = Build.VERSION.SDK_INT.toString()),
        sdk = SDKInfo(platform = "android", version = BuildConfig.FALU_VERSION_NAME),
        app = AppInfo(
            id = appContext.packageName,
            version = appContext.appVersionCode,
            name = appContext.appName.toString()
        ),
        device = DeviceInfo(type = "${Build.MANUFACTURER}_${Build.BRAND}_${Build.MODEL}"),
        plugin = PluginInfo(type = NATIVE_PLUGIN_TYPE),
        metadata = params,
        origin = origin
    )

    companion object {
        private const val NATIVE_PLUGIN_TYPE = "native"
    }
}