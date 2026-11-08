package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = BrightGreen,
    onPrimary = SolidBlack,
    primaryContainer = BrightGreenDark,
    onPrimaryContainer = Color.White,
    secondary = BrightRed,
    onSecondary = Color.White,
    secondaryContainer = BrightRedDark,
    onSecondaryContainer = Color.White,
    tertiary = CyberCyan,
    onTertiary = SolidBlack,
    background = SolidBlack,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = BrightRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
