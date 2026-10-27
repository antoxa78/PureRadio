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

// Distinct from Modern Blue: electric cyan on true black (was almost identical before).
@OptIn(ExperimentalTvMaterial3Api::class)
private val BlueNeonColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = ElectricBlueSoft,
    tertiary = ElectricBluePale,
    background = OledBlack,
    surface = Color(0xFF05080D),
    surfaceVariant = Color(0xFF0E1822),
    onPrimary = Black,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = ElectricBlue
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val OledCrimsonColorScheme = darkColorScheme(
    primary = Crimson,
    secondary = CrimsonSoft,
    tertiary = CrimsonPale,
    background = OledBlack,
    surface = Color(0xFF0C0507),
    surfaceVariant = Color(0xFF1A0A0E),
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Crimson
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val OledEmeraldColorScheme = darkColorScheme(
    primary = Emerald,
    secondary = EmeraldSoft,
    tertiary = EmeraldPale,
    background = OledBlack,
    surface = Color(0xFF04120C),
    surfaceVariant = Color(0xFF0C2018),
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = Emerald
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val OledMagentaColorScheme = darkColorScheme(
    primary = Magenta,
    secondary = MagentaSoft,
    tertiary = MagentaPale,
    background = OledBlack,
    surface = Color(0xFF0D0510),
    surfaceVariant = Color(0xFF1B0A21),
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = MagentaDark,
    onPrimaryContainer = Magenta
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val OledAmberColorScheme = darkColorScheme(
    primary = AmberNeon,
    secondary = AmberSoft,
    tertiary = AmberPale,
    background = OledBlack,
    surface = Color(0xFF0D0A02),
    surfaceVariant = Color(0xFF1E1704),
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = AmberDark,
    onPrimaryContainer = AmberNeon
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
private val ModernBlueColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = Color(0xFF0A0E14),
    surface = Color(0xFF151B24),
    surfaceVariant = Color(0xFF1F2933),
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = BlueDark,
    onPrimaryContainer = BluePrimary
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PureRadioTheme(
    theme: AppTheme = AppTheme.ModernBlue,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        AppTheme.ModernBlue -> ModernBlueColorScheme
        AppTheme.RetroGold -> RetroGoldColorScheme
        AppTheme.BlueNeon -> BlueNeonColorScheme
        AppTheme.Violet -> VioletColorScheme
        AppTheme.Monochrome -> MonochromeColorScheme
        AppTheme.Forest -> ForestColorScheme
        AppTheme.Contrast -> ContrastColorScheme
        AppTheme.OledCrimson -> OledCrimsonColorScheme
        AppTheme.OledEmerald -> OledEmeraldColorScheme
        AppTheme.OledMagenta -> OledMagentaColorScheme
        AppTheme.OledAmber -> OledAmberColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
