package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldenFire,
    secondary = CyberCyan,
    tertiary = DragonCrimson,
    background = ObsidianBlack,
    surface = CharcoalDark,
    surfaceVariant = SlateGray,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFC0C6E4)
)

private val LightColorScheme = lightColorScheme(
    primary = GoldenFire,
    secondary = CyberCyan,
    tertiary = DragonCrimson,
    background = PureWhiteBg,
    surface = SoftWhiteCard,
    surfaceVariant = DividerLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary,
    onSurfaceVariant = TextDarkSecondary
)

@Composable
fun DragonTheme(
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
