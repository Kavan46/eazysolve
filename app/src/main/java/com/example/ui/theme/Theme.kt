package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Material 3 Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = PureWhite,
    primaryContainer = PrimaryIndigoContainer,
    onPrimaryContainer = PrimaryIndigoDark,
    inversePrimary = PrimaryIndigoLight,
    secondary = Slate900,
    onSecondary = PureWhite,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate800,
    tertiary = AccentOrange,
    onTertiary = PureWhite,
    tertiaryContainer = AccentOrangeBg,
    onTertiaryContainer = AccentOrange,
    background = PureWhite,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate600,
    surfaceTint = PrimaryIndigo,
    inverseSurface = Slate900,
    inverseOnSurface = PureWhite,
    outline = Slate200,
    outlineVariant = Slate100,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// Material 3 Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = DarkBackground,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFEEF2FF),
    inversePrimary = PrimaryIndigo,
    secondary = Slate100,
    onSecondary = Slate900,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = AccentAmber,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = PrimaryIndigoLight,
    inverseSurface = PureWhite,
    inverseOnSurface = Slate900,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFF87171),
    onError = DarkBackground,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

// Extended Custom Palette for Theme-Adaptive Components
data class ExtendedColors(
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val cardBorder: Color,
    val subtleBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val pastelIndigo: Color,
    val pastelEmerald: Color,
    val pastelAmber: Color,
    val pastelRose: Color,
    val pastelViolet: Color,
    val pastelSky: Color,
    val pastelOrange: Color,
    val isDark: Boolean
)

val LightExtendedColors = ExtendedColors(
    cardBackground = Slate50,
    cardBackgroundElevated = PureWhite,
    cardBorder = Slate100,
    subtleBorder = Slate200,
    textPrimary = Slate900,
    textSecondary = Slate500,
    textMuted = Slate400,
    pastelIndigo = PastelIndigo,
    pastelEmerald = PastelEmerald,
    pastelAmber = PastelAmber,
    pastelRose = PastelRose,
    pastelViolet = PastelViolet,
    pastelSky = PastelSky,
    pastelOrange = PastelOrange,
    isDark = false
)

val DarkExtendedColors = ExtendedColors(
    cardBackground = DarkSurface,
    cardBackgroundElevated = DarkSurfaceVariant,
    cardBorder = DarkBorder,
    subtleBorder = Color(0xFF334155),
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    pastelIndigo = Color(0xFF1E1B4B),
    pastelEmerald = Color(0xFF064E3B),
    pastelAmber = Color(0xFF451A03),
    pastelRose = Color(0xFF4C0519),
    pastelViolet = Color(0xFF3B0764),
    pastelSky = Color(0xFF0C4A6E),
    pastelOrange = Color(0xFF431407),
    isDark = true
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val GeometricShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EazySolveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = GeometricShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    EazySolveTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
