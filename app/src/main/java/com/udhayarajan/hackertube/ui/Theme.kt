package com.udhayarajan.hackertube.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFFFF6600)

private val Dark = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Color(0xFF3EA6FF),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFF1F1F1),
    surface = Color(0xFF0F0F0F),
    onSurface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFF272727),
    onSurfaceVariant = Color(0xFFAAAAAA),
    surfaceContainer = Color(0xFF212121),
    surfaceContainerLow = Color(0xFF181818),
    surfaceContainerHigh = Color(0xFF272727),
    secondaryContainer = Color(0xFF3F3F3F),
    onSecondaryContainer = Color(0xFFF1F1F1),
    outline = Color(0xFF303030),
    outlineVariant = Color(0xFF303030),
)

private val Light = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Color(0xFF065FD4),
    background = Color.White,
    onBackground = Color(0xFF0F0F0F),
    surface = Color.White,
    onSurface = Color(0xFF0F0F0F),
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF606060),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainerHigh = Color(0xFFE5E5E5),
    secondaryContainer = Color(0xFFE5E5E5),
    onSecondaryContainer = Color(0xFF0F0F0F),
    outline = Color(0xFFE5E5E5),
    outlineVariant = Color(0xFFE5E5E5),
)

@Composable
fun HackerTubeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
