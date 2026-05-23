package com.toxa.pureradio.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.toxa.pureradio.ui.viewmodel.AppTheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val RetroGoldColorScheme = darkColorScheme(
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
private val BlueNeonColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = Black,
    surface = DarkGrey,
    surfaceVariant = SurfaceGrey,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = BlueDark,
    onPrimaryContainer = BluePrimary
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val VioletColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF9C27B0),
    tertiary = Color(0xFFCE93D8),
    background = Black,
    surface = DarkGrey,
    surfaceVariant = SurfaceGrey,
    onPrimary = Black,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = Color(0xFF1A0D2E),
    onPrimaryContainer = Color(0xFFBB86FC)
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val MonochromeColorScheme = darkColorScheme(
    primary = Grey,
    secondary = Color(0xFFBDBDBD),
    tertiary = Color(0xFFE0E0E0),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Grey
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF2E7D32),
    tertiary = Color(0xFF81C784),
    background = Black,
    surface = DarkGrey,
    surfaceVariant = SurfaceGrey,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = ForestDark,
    onPrimaryContainer = Color(0xFF4CAF50)
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val ContrastColorScheme = darkColorScheme(
    primary = White,
    secondary = Color(0xFFBDBDBD),
    tertiary = Grey,
    background = Color(0xFF000000),
    surface = Color(0xFF111111),
    surfaceVariant = Color(0xFF222222),
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = White
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PureRadioTheme(
    theme: AppTheme = AppTheme.RetroGold,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        AppTheme.RetroGold -> RetroGoldColorScheme
        AppTheme.BlueNeon -> BlueNeonColorScheme
        AppTheme.Violet -> VioletColorScheme
        AppTheme.Monochrome -> MonochromeColorScheme
        AppTheme.Forest -> ForestColorScheme
        AppTheme.Contrast -> ContrastColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
