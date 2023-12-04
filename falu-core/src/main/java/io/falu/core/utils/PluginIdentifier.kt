package io.falu.core.utils

import android.util.Log
import androidx.annotation.RestrictTo

/**
 * Identify which plugin from which the SDK is being used.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object PluginIdentifier {
    private val TAG = PluginIdentifier::class.java.simpleName

    /**
     * Identify the plugin.
     */
    val pluginType: String? = PluginType.values().firstOrNull {
        detectSDK(it.className)
    }?.pluginName

    private fun detectSDK(className: String): Boolean = try {
        Class.forName(className)
        true
    } catch (e: ClassNotFoundException) {
        Log.d(TAG, "$className not found: $e")
        false
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    enum class PluginType(
        val className: String,
        val pluginName: String
    ) {
        CORDOVA("org.apache.cordova.CordovaActivity", "cordova"),
        FLUTTER("io.flutter.embedding.engine.FlutterEngine", "flutter"),
        REACT_NATIVE("com.facebook.react.bridge.NativeModule", "react-native"),
        UNITY("com.unity3d.player.UnityPlayerActivity", "unity")
    }
}