package com.gymapp.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary                = md_theme_dark_primary,
    onPrimary              = md_theme_dark_onPrimary,
    primaryContainer       = md_theme_dark_primaryContainer,
    onPrimaryContainer     = md_theme_dark_onPrimaryContainer,
    secondary              = md_theme_dark_secondary,
    onSecondary            = md_theme_dark_onSecondary,
    background             = md_theme_dark_background,
    onBackground           = md_theme_dark_onBackground,
    surface                = md_theme_dark_surface,
    onSurface              = md_theme_dark_onSurface,
    surfaceVariant         = md_theme_dark_surfaceVariant,
    onSurfaceVariant       = md_theme_dark_onSurfaceVariant,
    outline                = md_theme_dark_outline,
    outlineVariant         = md_theme_dark_outlineVariant,
    error                  = md_theme_dark_error,
    onError                = md_theme_dark_onError
)

@Composable
fun GymAppTheme(
    darkTheme: Boolean = true, // ALWAYS dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
