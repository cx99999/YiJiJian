package com.example.piecework.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppBlue = Color(0xFF1677FF)
val AppOrange = AppBlue
val AppBackground = Color(0xFFF4F7FB)
val AppCardRadius = 12.dp

private val LightColorScheme = lightColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    secondary = Color(0xFF24415F),
    background = AppBackground,
    onBackground = Color(0xFF222222),
    surface = Color.White,
    onSurface = Color(0xFF222222)
)

@Composable
fun PieceworkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
