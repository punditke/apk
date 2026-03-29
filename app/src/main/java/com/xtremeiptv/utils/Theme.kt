package com.xtremeiptv.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF50C878),
    onPrimary = Color(0xFF0A1A12),
    primaryContainer = Color(0xFF1B4D3E),
    onPrimaryContainer = Color(0xFF50C878),
    secondary = Color(0xFF2D6A4F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF1B4D3E),
    onSecondaryContainer = Color(0xFF50C878),
    background = Color(0xFF0A1A12),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D1F16),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1B4D3E),
    onSurfaceVariant = Color(0xFFB0BEC5),
    error = Color(0xFFCF6679),
    onError = Color(0xFF0A1A12)
)

@Composable
fun XtremeIPTVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmeraldDarkColorScheme,
        typography = Typography(),
        content = content
    )
}
