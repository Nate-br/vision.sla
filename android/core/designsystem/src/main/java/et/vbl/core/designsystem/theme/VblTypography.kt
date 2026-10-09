package et.vbl.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Font families ────────────────────────────────────────────────────────────
// Noto Sans Ethiopic: full Ge'ez/Amharic support (add to res/font/ in Phase 0 polish)
// Noto Sans: companion Latin family
// TODO(Phase 0): Download Noto Sans Ethiopic and Noto Sans from Google Fonts,
//   place .ttf files in android/core/designsystem/src/main/res/font/ and
//   replace these FontFamily.Default placeholders.

val NotoSansEthiopic = FontFamily.Default   // TODO: replace with real font
val NotoSans         = FontFamily.Default   // TODO: replace with real font

/**
 * VBL Typography — Material 3 scale mapped to our design brief.
 *
 * Font size convention:
 *  - Use sp units (respects user's font scale up to 200% per plan requirement)
 *  - Body text ≥ 14sp, captions ≥ 12sp
 *  - All amounts displayed as ETB 1,500.00 (see VblFormatters.kt)
 */
val VblTypography = Typography(
    displayLarge  = TextStyle(fontFamily = NotoSansEthiopic, fontWeight = FontWeight.Bold,    fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = NotoSansEthiopic, fontWeight = FontWeight.Bold,    fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontFamily = NotoSansEthiopic, fontWeight = FontWeight.Bold,    fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = NotoSansEthiopic, fontWeight = FontWeight.SemiBold,fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontFamily = NotoSansEthiopic, fontWeight = FontWeight.SemiBold,fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = NotoSansEthiopic, fontWeight = FontWeight.SemiBold,fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.SemiBold,fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium   = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall    = TextStyle(fontFamily = NotoSans,         fontWeight = FontWeight.Medium,  fontSize = 11.sp, lineHeight = 16.sp),
)
