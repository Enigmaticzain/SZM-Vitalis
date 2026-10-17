package com.example.humandigitaltwin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,

    secondary = CyanSecondary,
    onSecondary = CyanOnSecondary,
    secondaryContainer = CyanSecondaryContainer,
    onSecondaryContainer = CyanOnSecondaryContainer,

    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,

    background = CharcoalDark,
    onBackground = TextHighEmphasis,

    surface = CharcoalSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextMediumEmphasis,
    surfaceContainer = CharcoalSurfaceContainer,
    surfaceContainerLow = CharcoalSurfaceContainerLow,
    surfaceContainerHigh = CharcoalSurfaceContainerHigh,

    outline = OutlineBorder,
    outlineVariant = OutlineVariant,

    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0xFF4E1616),
    onErrorContainer = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = LightTealPrimary,
    onPrimary = LightTealOnPrimary,
    primaryContainer = LightTealPrimaryContainer,
    onPrimaryContainer = LightTealOnPrimaryContainer,

    secondary = LightCyanSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,

    background = LightBackground,
    onBackground = Color(0xFF191C1D),

    surface = LightSurface,
    onSurface = Color(0xFF191C1D),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF3F4947),
    surfaceContainer = Color(0xFFF0F4F3),
    surfaceContainerLow = Color(0xFFE9EFEB),
    surfaceContainerHigh = Color(0xFFE2E9E5),

    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBFC9C6),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun HumanDigitalTwinTheme(
    darkTheme: Boolean = true, // Default to clinical dark theme for premium bio-dashboard
    dynamicColor: Boolean = false, // Keep consistent branding identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}