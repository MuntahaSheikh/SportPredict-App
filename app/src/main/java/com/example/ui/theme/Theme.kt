package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PremiumColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = PremiumDarkBg,
    secondary = AccentBlue,
    onSecondary = TextPrimary,
    tertiary = AccentPink,
    background = PremiumDarkBg,
    onBackground = TextPrimary,
    surface = PremiumDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = PremiumDarkCard,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumColorScheme,
        typography = Typography,
        content = content
    )
}
