package io.falu.identity.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.res.getColorOrThrow
import io.falu.identity.R
import java.lang.reflect.Method

/**
 * The theme attempts to read the them from the hosting app's context
 */
@Composable
internal fun IdentityTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val key = context.theme.key ?: context.theme
    val layoutDirection = LocalLayoutDirection.current

    val themeParameters = remember(key) {
        createTheme(context, layoutDirection)
    }

    val hostingAppTypography = themeParameters.typography ?: MaterialTheme.typography
    val hostingAppShapes = themeParameters.shapes ?: MaterialTheme.shapes

    MaterialTheme(
        colorScheme = themeParameters.colorScheme ?: MaterialTheme.colorScheme,
        shapes = hostingAppShapes
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content
        )
    }
}

/**
 * Copied from Mdc3Theme.kt
 */
private fun createTheme(
    context: Context,
    layoutDirection: LayoutDirection,
    readColorScheme: Boolean = true,
    readTypography: Boolean = true
): ThemeParameters {
    return context.obtainStyledAttributes(R.styleable.ThemeAdapterMaterialTheme).use { ta ->
        val colorScheme: ColorScheme? = if (readColorScheme) {
            val primary = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorPrimary)
            val onPrimary = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnPrimary)
            val primaryInverse = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorPrimaryInverse)
            val primaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorPrimaryContainer)
            val onPrimaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnPrimaryContainer)
            val secondary = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorSecondary)
            val onSecondary = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnSecondary)
            val secondaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorSecondaryContainer)
            val onSecondaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnSecondaryContainer)
            val tertiary = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorTertiary)
            val onTertiary = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnTertiary)
            val tertiaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorTertiaryContainer)
            val onTertiaryContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnTertiaryContainer)
            val background = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_android_colorBackground)
            val onBackground = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnBackground)
            val surface = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorSurface)
            val onSurface = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnSurface)
            val surfaceVariant = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorSurfaceVariant)
            val onSurfaceVariant = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnSurfaceVariant)
            val elevationOverlay = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_elevationOverlayColor)
            val surfaceInverse = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorSurfaceInverse)
            val onSurfaceInverse = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnSurfaceInverse)
            val outline = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOutline)
            val outlineVariant = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOutlineVariant)
            val error = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorError)
            val onError = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnError)
            val errorContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorErrorContainer)
            val onErrorContainer = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_colorOnErrorContainer)
            val scrimBackground = ta.parseColor(R.styleable.ThemeAdapterMaterialTheme_scrimBackground)

            val isLightTheme = ta.getBoolean(R.styleable.ThemeAdapterMaterialTheme_isLightTheme, true)

            if (isLightTheme) {
                lightColorScheme(
                    primary = primary,
                    onPrimary = onPrimary,
                    inversePrimary = primaryInverse,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    secondaryContainer = secondaryContainer,
                    onSecondaryContainer = onSecondaryContainer,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    tertiaryContainer = tertiaryContainer,
                    onTertiaryContainer = onTertiaryContainer,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    surfaceVariant = surfaceVariant,
                    onSurfaceVariant = onSurfaceVariant,
                    surfaceTint = elevationOverlay,
                    inverseSurface = surfaceInverse,
                    inverseOnSurface = onSurfaceInverse,
                    outline = outline,
                    outlineVariant = outlineVariant,
                    error = error,
                    onError = onError,
                    errorContainer = errorContainer,
                    onErrorContainer = onErrorContainer,
                    scrim = scrimBackground
                )
            } else {
                darkColorScheme(
                    primary = primary,
                    onPrimary = onPrimary,
                    inversePrimary = primaryInverse,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    secondaryContainer = secondaryContainer,
                    onSecondaryContainer = onSecondaryContainer,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    tertiaryContainer = tertiaryContainer,
                    onTertiaryContainer = onTertiaryContainer,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    surfaceVariant = surfaceVariant,
                    onSurfaceVariant = onSurfaceVariant,
                    surfaceTint = elevationOverlay,
                    inverseSurface = surfaceInverse,
                    inverseOnSurface = onSurfaceInverse,
                    outline = outline,
                    outlineVariant = outlineVariant,
                    error = error,
                    onError = onError,
                    errorContainer = errorContainer,
                    onErrorContainer = onErrorContainer,
                    scrim = scrimBackground
                )
            }
        } else {
            null
        }
        // Extract typography if readTypography is true

        // You can extract shapes similarly if needed or return default shapes
        val shapes: Shapes = Shapes()

        ThemeParameters(colorScheme, null, shapes)
    }
}

/**
 * This class contains the individual components of a [MaterialTheme]: [ColorScheme] and
 * [Typography].
 */
private data class ThemeParameters(
    val colorScheme: ColorScheme?,
    val typography: Typography?,
    val shapes: Shapes?
)

/**
 * Copied from Mdc3Theme.kt
 */
private inline val Resources.Theme.key: Any?
    @SuppressLint("PrivateApi")
    get() {
        if (!sThemeGetKeyMethodFetched) {
            try {
                @Suppress("SoonBlockedPrivateApi")
                sThemeGetKeyMethod = Resources.Theme::class.java.getDeclaredMethod("getKey")
                    .apply { isAccessible = true }
            } catch (e: ReflectiveOperationException) {
                // Failed to retrieve Theme.getKey method
            }
            sThemeGetKeyMethodFetched = true
        }
        if (sThemeGetKeyMethod != null) {
            return try {
                sThemeGetKeyMethod?.invoke(this)
            } catch (e: ReflectiveOperationException) {
                // Failed to invoke Theme.getKey()
            }
        }
        return null
    }

private fun TypedArray.parseColor(
    index: Int,
    fallbackColor: Color = Color.Unspecified
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

private var sThemeGetKeyMethodFetched = false
private var sThemeGetKeyMethod: Method? = null