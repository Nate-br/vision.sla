package et.vbl.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ─── VBL Colour Palette ───────────────────────────────────────────────────────
// Primary: deep trust-blue  (#1A4B8C)
// Secondary: warm gold accent (#C8872A) — SLA financial warmth
// Error: calm destructive (#B3261E)
// Background: very dark navy (#0D1B2A) for dark theme

private val VblDarkColorScheme = darkColorScheme(
    primary          = VblColors.PrimaryBlue,
    onPrimary        = VblColors.White,
    primaryContainer = VblColors.PrimaryBlueDark,
    secondary        = VblColors.GoldAccent,
    onSecondary      = VblColors.NavyDark,
    background       = VblColors.NavyDark,
    onBackground     = VblColors.White,
    surface          = VblColors.SurfaceDark,
    onSurface        = VblColors.OnSurfaceDark,
    error            = VblColors.Error,
    onError          = VblColors.White,
    outline          = VblColors.Outline,
)

private val VblLightColorScheme = lightColorScheme(
    primary          = VblColors.PrimaryBlue,
    onPrimary        = VblColors.White,
    primaryContainer = VblColors.PrimaryBlueLight,
    secondary        = VblColors.GoldAccent,
    onSecondary      = VblColors.White,
    background       = VblColors.BackgroundLight,
    onBackground     = VblColors.NavyDark,
    surface          = VblColors.White,
    onSurface        = VblColors.NavyDark,
    error            = VblColors.Error,
    onError          = VblColors.White,
    outline          = VblColors.OutlineLight,
)

/**
 * VBL Design System theme.
 *
 * Uses Material 3 with a custom token set matching the plan's design brief:
 * calm, trustworthy, clear. Dark theme is the default for OLED screens common
 * in the Ethiopian market.
 */
@Composable
fun VblTheme(
    darkTheme: Boolean = true, // dark default
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) VblDarkColorScheme else VblLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = VblTypography,
        shapes      = VblShapes,
        content     = content,
    )
}
