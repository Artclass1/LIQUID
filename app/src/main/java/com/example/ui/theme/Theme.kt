package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElegantPurplePrimary,
    secondary = ElegantMutedText,
    tertiary = ElegantCoralAccent,
    background = ElegantDarkBg,
    surface = ElegantSurfaceCard,
    surfaceVariant = ElegantSurfaceRow,
    primaryContainer = ElegantPurpleContainer,
    secondaryContainer = ElegantSurfaceRow,
    onPrimary = ElegantPurpleDark,
    onSecondary = ElegantDarkBg,
    onTertiary = ElegantCoralDark,
    onBackground = ElegantLightText,
    onSurface = ElegantLightText,
    onPrimaryContainer = ElegantLightText,
    onSecondaryContainer = ElegantMutedText,
    onSurfaceVariant = ElegantMutedText,
    outline = ElegantDeepMutedText,
    outlineVariant = ElegantSurfaceChip
)

private val LightColorScheme = darkColorScheme( // Force dark theme layout experience even in light mode request to respect "Elegant Dark" intent completely
    primary = ElegantPurplePrimary,
    secondary = ElegantMutedText,
    tertiary = ElegantCoralAccent,
    background = ElegantDarkBg,
    surface = ElegantSurfaceCard,
    surfaceVariant = ElegantSurfaceRow,
    primaryContainer = ElegantPurpleContainer,
    secondaryContainer = ElegantSurfaceRow,
    onPrimary = ElegantPurpleDark,
    onSecondary = ElegantDarkBg,
    onTertiary = ElegantCoralDark,
    onBackground = ElegantLightText,
    onSurface = ElegantLightText,
    onPrimaryContainer = ElegantLightText,
    onSecondaryContainer = ElegantMutedText,
    onSurfaceVariant = ElegantMutedText,
    outline = ElegantDeepMutedText,
    outlineVariant = ElegantSurfaceChip
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // We explicitly disable dynamicColor system overrides to guarantee the app displays our 
    // highly customized, premium, industrial Trade Jade and Metallic Gold branding perfectly on all screens.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
