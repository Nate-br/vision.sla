package et.vbl.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * VBL Design System colour tokens.
 *
 * Naming convention: semantic name, not hex value.
 * These are raw tokens — use MaterialTheme.colorScheme in composables, not these directly,
 * unless you are building the colour scheme itself.
 */
object VblColors {
    // ─── Brand ──────────────────────────────────────────────────────────────
    /** Deep trust-blue — primary actions, key UI elements */
    val PrimaryBlue      = Color(0xFF1A4B8C)
    val PrimaryBlueDark  = Color(0xFF0F2D5A)
    val PrimaryBlueLight = Color(0xFFD0E4FF)

    /** Warm gold — accent, CTAs on dark, success highlights */
    val GoldAccent       = Color(0xFFC8872A)
    val GoldLight        = Color(0xFFFFD488)

    // ─── Neutrals ───────────────────────────────────────────────────────────
    val NavyDark         = Color(0xFF0D1B2A)
    val SurfaceDark      = Color(0xFF152338)
    val OnSurfaceDark    = Color(0xFFCFD8E3)
    val BackgroundLight  = Color(0xFFF5F8FC)
    val White            = Color(0xFFFFFFFF)

    // ─── Semantic ───────────────────────────────────────────────────────────
    /** WCAG AA compliant — always pair with icon + text, never colour alone */
    val Success          = Color(0xFF2E7D32)
    val SuccessLight     = Color(0xFFE8F5E9)
    val Warning          = Color(0xFFF57C00)
    val WarningLight     = Color(0xFFFFF3E0)
    val Error            = Color(0xFFB3261E)
    val ErrorLight       = Color(0xFFFCEEEC)

    // ─── Outlines ───────────────────────────────────────────────────────────
    val Outline          = Color(0xFF3A5070)
    val OutlineLight     = Color(0xFFB0C4DE)
}
