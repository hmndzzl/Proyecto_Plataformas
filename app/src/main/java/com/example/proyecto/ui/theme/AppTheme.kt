package com.example.proyecto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


// UVG Colors
private val UVGGreen = Color(0xFF006837)
private val UVGGreenDark = Color(0xFF00502a)
private val UVGGreenLight = Color(0xFF4CAF50)

private val UVGBlue = Color(0xFF1a4f8a)
private val UVGBlueDark = Color(0xFF154270)
private val UVGBlueLight = Color(0xFF2196F3)

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = UVGGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF00502a),

    secondary = UVGBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = UVGBlueDark,

    tertiary = Color(0xFF7C4DFF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE7F6),
    onTertiaryContainer = Color(0xFF4A148C),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),

    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF1F2937),

    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF0F4F8),
    onSurfaceVariant = Color(0xFF6B7280),

    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF3F4F6)
)

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = UVGGreenLight,
    onPrimary = Color(0xFF003919),
    primaryContainer = Color(0xFF00502a),
    onPrimaryContainer = Color(0xFFDCFCE7),

    secondary = UVGBlueLight,
    onSecondary = Color(0xFF0C2340),
    secondaryContainer = UVGBlueDark,
    onSecondaryContainer = Color(0xFFDBEAFE),

    tertiary = Color(0xFFB39DDB),
    onTertiary = Color(0xFF4A148C),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFEDE7F6),

    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFFDC2626),
    onErrorContainer = Color(0xFFFEE2E2),

    background = Color(0xFF121212),
    onBackground = Color(0xFFF9FAFB),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFD1D5DB),

    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF4B5563)
)

@Composable
fun UVGEspaciosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// Typography
private val AppTypography = Typography(
    // Display
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    displaySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),

    // Headline
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    ),

    // Title
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
    ),

    // Body
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    ),

    // Label
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    )
)