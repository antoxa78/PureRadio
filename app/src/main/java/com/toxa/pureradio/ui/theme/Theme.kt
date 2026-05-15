package com.toxa.pureradio.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkColorScheme = darkColorScheme(
    primary = RetroGold,
    secondary = Amber,
    tertiary = WoodBrown,
    background = Black,
    surface = DarkGrey,
    surfaceVariant = SurfaceGrey,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = White,
    onBackground = White,
    onSurface = White,
    primaryContainer = DarkWood,
    onPrimaryContainer = RetroGold
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PureRadioTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
