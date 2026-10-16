package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CollegeBluePrimary,
    onPrimary = Color.White,
    primaryContainer = CollegeBlueContainer,
    onPrimaryContainer = CollegeOnBlueContainer,
    secondary = CollegeBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = CollegeBlueLight,
    onTertiary = Color.White,
    background = CollegeBackground,
    onBackground = CollegeTextPrimary,
    surface = CollegeSurface,
    onSurface = CollegeTextPrimary,
    surfaceVariant = CollegeSurfaceVariant,
    onSurfaceVariant = CollegeTextSecondary,
    outline = CollegeOutline,
    error = StatusAbsent,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = CollegeBlueDarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = CollegeBlueDarkSecondary,
    onSecondary = Color(0xFF0F172A),
    background = CollegeDarkBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = CollegeDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = CollegeDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
