package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeMode

fun parseHexColor(hex: String, default: Color = HighDensityPrimary): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}

@Composable
fun MessengerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentHex: String = "#0B57D0",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val primaryColor = parseHexColor(accentHex, HighDensityPrimary)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = if (accentHex == "#0B57D0") HighDensityPrimaryDark else primaryColor,
            onPrimary = Color(0xFF00315B),
            primaryContainer = HighDensityPrimaryContainerDark,
            onPrimaryContainer = HighDensityOnPrimaryContainerDark,
            secondary = HighDensityTextSecondaryDark,
            onSecondary = Color(0xFFE2E2E6),
            background = HighDensityBackgroundDark,
            surface = HighDensitySurfaceDark,
            surfaceVariant = HighDensitySurfaceVariantDark,
            onSurfaceVariant = HighDensityTextPrimaryDark,
            outline = Color(0xFF44474E),
            outlineVariant = Color(0xFF2E3137),
            onBackground = HighDensityTextPrimaryDark,
            onSurface = HighDensityTextPrimaryDark
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = HighDensityPrimaryContainer,
            onPrimaryContainer = HighDensityOnPrimaryContainer,
            secondary = HighDensityTextSecondary,
            onSecondary = Color.White,
            background = HighDensityBackgroundLight,
            surface = HighDensitySurfaceLight,
            surfaceVariant = HighDensitySurfaceVariantLight,
            onSurfaceVariant = HighDensityTextPrimary,
            outline = HighDensityBorder,
            outlineVariant = Color(0xFFCBD5E1),
            onBackground = HighDensityTextPrimary,
            onSurface = HighDensityTextPrimary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
