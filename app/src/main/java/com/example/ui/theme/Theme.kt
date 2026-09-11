package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MinimalistDarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoDarkElevated,
    onPrimaryContainer = MonoWhite,
    secondary = MonoTextSecondaryDark,
    onSecondary = MonoWhite,
    secondaryContainer = MonoDarkElevated,
    onSecondaryContainer = MonoWhite,
    background = MonoOledDark,
    onBackground = MonoWhite,
    surface = MonoDarkSurface,
    onSurface = MonoWhite,
    surfaceVariant = MonoDarkElevated,
    onSurfaceVariant = MonoTextSecondaryDark,
    outline = MonoDarkBorder,
    error = UrgentRed,
    onError = MonoWhite
)

val MinimalistOledColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = Color(0xFF121212),
    onPrimaryContainer = MonoWhite,
    secondary = MonoMutedSilver,
    onSecondary = MonoWhite,
    secondaryContainer = Color(0xFF181818),
    onSecondaryContainer = MonoWhite,
    background = MonoBlack,
    onBackground = MonoWhite,
    surface = MonoBlack,
    onSurface = MonoWhite,
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = MonoTextSecondaryDark,
    outline = Color(0xFF222222),
    error = UrgentRed,
    onError = MonoWhite
)

val MinimalistLightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoLightElevated,
    onPrimaryContainer = MonoBlack,
    secondary = MonoTextSecondaryLight,
    onSecondary = MonoWhite,
    secondaryContainer = MonoLightElevated,
    onSecondaryContainer = MonoBlack,
    background = MonoLightBg,
    onBackground = MonoBlack,
    surface = MonoLightSurface,
    onSurface = MonoBlack,
    surfaceVariant = MonoLightElevated,
    onSurfaceVariant = MonoTextSecondaryLight,
    outline = MonoLightBorder,
    error = UrgentRed,
    onError = MonoWhite
)

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    OLED_BLACK
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.OLED_BLACK,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> MinimalistLightColorScheme
        AppThemeMode.DARK -> MinimalistDarkColorScheme
        AppThemeMode.OLED_BLACK -> MinimalistOledColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) MinimalistOledColorScheme else MinimalistLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

