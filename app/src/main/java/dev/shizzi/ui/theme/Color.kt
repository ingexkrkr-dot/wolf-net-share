package dev.shizzi.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ShizziColors(
    val primary: Color,
    val onPrimary: Color,

    val primaryBright: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val border: Color,
    val shadow: Color,
    val isDark: Boolean,
    val primaryContainer: Color = primary,
    val onPrimaryContainer: Color = onPrimary,
    val secondary: Color = primary,
    val tertiary: Color = primaryBright,
    val surfaceVariant: Color = surface,
    val surfaceContainer: Color = surface,
)

private val OnPrimary = Color(0xFF000000)

val LightColors = ShizziColors(
    primary = Color(0xFF168BFF),
    onPrimary = OnPrimary,
    primaryBright = Color(0xFF70DFFF),
    background = Color(0xFFF3F8FF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF050B1D),
    onSurfaceMuted = Color(0xFF526B91),
    border = Color(0xFF000000),
    shadow = Color(0xFF000000),
    isDark = false,
)

val DarkColors = ShizziColors(
    primary = Color(0xFF29C8FF),
    onPrimary = OnPrimary,

    primaryBright = Color(0xFF9BEAFF),
    background = Color(0xFF050B1D),
    surface = Color(0xFF081735),
    onSurface = Color(0xFFF3F8FF),
    onSurfaceMuted = Color(0xFF9DB5D8),
    border = Color(0xFFFFFFFF),
    shadow = Color(0xFFFFFFFF),
    isDark = true,
)
