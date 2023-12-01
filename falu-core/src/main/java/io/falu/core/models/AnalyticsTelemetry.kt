package io.falu.core.models

import androidx.annotation.RestrictTo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import software.tingle.api.adapters.ISO8601DateAdapter
import java.util.Date
import java.util.UUID

/**
 * This is reserved for SDKs with UI and not for server integrations.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AnalyticsTelemetry(
    /**
     * Identifier of the event used for de-duplication.
     */
    val id: String = UUID.randomUUID().toString(),

    /**
     * Name of the event.
     */
    val name: String,

    /**
     * Identifier of the client.
     */
    val client: String,

    /**
     * Time at which the object was created.
     */
    @JsonAdapter(ISO8601DateAdapter::class)
    val created: Date = Date(),

    /**
     */
    @SerializedName("os")
    val os: OSInfo,

    /**
     */
    @SerializedName("device")
    val device: DeviceInfo,

    /**
     */
    @SerializedName("app")
    val app: AppInfo,

    /**
     */
    @SerializedName("plugin")
    val plugin: PluginInfo,

    /**
     */
    @SerializedName("sdk")
    val sdk: SDKInfo,

    /**
     *
     */
    val metadata: Map<String, *>,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class OSInfo(
    /**
     */
    val version: String
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class DeviceInfo(
    /**
     */
    val type: String
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class PluginInfo(
    /**
     */
    val type: String
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class SDKInfo(
    /**
     */
    val platform: String,

    /**
     */
    val version: String
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AppInfo(
    /**
     */
    val id: String,

    /**
     */
    val name: String,

    /**
     */
    val version: String
)

