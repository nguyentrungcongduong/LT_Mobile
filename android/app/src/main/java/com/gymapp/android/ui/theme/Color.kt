package com.gymapp.android.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark Theme (Primary – #121212 + Orange Glow) ─────────────────────────────
val DarkBackground      = Color(0xFF121212)
val DarkSurface         = Color(0xFF1C1C1E)
val DarkSurface2        = Color(0xFF252528)
val DarkSurface3        = Color(0xFF2C2C30)

val OrangePrimary       = Color(0xFFFF6B2B)   // warm orange
val OrangeLight         = Color(0xFFFF8C00)
val OrangeGlow          = Color(0xFFFF5722)
val OrangeContainer     = Color(0xFF3A1A08)

val TextPrimary         = Color(0xFFF2F2F2)
val TextSecondary       = Color(0xFF9A9A9E)
val TextHint            = Color(0xFF505055)

val OutlineDark         = Color(0xFF2A2A2E)
val OutlineVariantDark  = Color(0xFF1E1E22)

val ErrorColor          = Color(0xFFE53935)
val SuccessColor        = Color(0xFF4CAF50)
val WarningColor        = Color(0xFFFFC107)

// ── MaterialTheme token aliases ───────────────────────────────────────────────
val md_theme_dark_primary              = OrangePrimary
val md_theme_dark_onPrimary            = Color(0xFFFFFFFF)
val md_theme_dark_primaryContainer     = OrangeContainer
val md_theme_dark_onPrimaryContainer   = Color(0xFFFFCCBC)
val md_theme_dark_secondary            = Color(0xFFB0BEC5)
val md_theme_dark_onSecondary          = Color(0xFF0D0D0D)
val md_theme_dark_background           = DarkBackground
val md_theme_dark_onBackground         = TextPrimary
val md_theme_dark_surface              = DarkSurface
val md_theme_dark_onSurface            = TextPrimary
val md_theme_dark_surfaceVariant       = DarkSurface2
val md_theme_dark_onSurfaceVariant     = TextSecondary
val md_theme_dark_outline              = OutlineDark
val md_theme_dark_outlineVariant       = OutlineVariantDark
val md_theme_dark_error                = ErrorColor
val md_theme_dark_onError              = Color(0xFFFFFFFF)

// ── Light kept for compatibility (unused) ─────────────────────────────────────
val md_theme_light_primary             = OrangePrimary
val md_theme_light_onPrimary           = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer    = Color(0xFFFFCCBC)
val md_theme_light_onPrimaryContainer  = Color(0xFF3D1A0A)
val md_theme_light_secondary           = Color(0xFF546E7A)
val md_theme_light_onSecondary         = Color(0xFFFFFFFF)
val md_theme_light_background          = Color(0xFFFFFFFF)
val md_theme_light_onBackground        = Color(0xFF1A1A1A)
val md_theme_light_surface             = Color(0xFFF8F9FA)
val md_theme_light_onSurface           = Color(0xFF1A1A1A)
val md_theme_light_surfaceVariant      = Color(0xFFF1F3F4)
val md_theme_light_onSurfaceVariant    = Color(0xFF5F6368)
val md_theme_light_outline             = Color(0xFFDADCE0)
val md_theme_light_outlineVariant      = Color(0xFFEBEBEB)
val md_theme_light_error               = Color(0xFFD93025)
val md_theme_light_onError             = Color(0xFFFFFFFF)

val color_success = SuccessColor
val color_warning = WarningColor
