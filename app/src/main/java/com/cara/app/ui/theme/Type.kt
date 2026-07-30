package com.cara.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: swap these placeholders for the real typefaces from CLAUDE_android.md —
// Fraunces (display), Inter (body), JetBrains Mono (numeric/data). Either bundle
// .ttf files under res/font/ or wire up Google Fonts downloadable fonts. Sizes
// and weights below are already set per spec so layout won't reflow once the
// real fonts are swapped in.
val DisplayFontFamily = FontFamily.Serif // stand-in for Fraunces
val BodyFontFamily = FontFamily.Default // stand-in for Inter
val MonoFontFamily = FontFamily.Monospace // stand-in for JetBrains Mono

val CaraTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal, fontSize = 40.sp),
    displayMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = DisplayFontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = BodyFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    // Reserved for distance/price/rating/timestamp figures per the design spec —
    // reach for this style specifically wherever a "live measurement" is rendered.
    labelSmall = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
)
