package com.cara.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design tokens — see CLAUDE_android.md "Color tokens". Every color used in the
// app should trace back to one of these; do not introduce ad-hoc hex values or
// stock Material colors elsewhere.
//
// InkBase/InkSurface/InkRaised/WarmWhite/WarmGrey resolve to a dark-theme or
// light-theme value depending on ThemeController.isDarkTheme, so every screen
// (which already reads these as plain top-level names) picks up the toggle
// automatically with no changes to screen code.

private val InkBaseDark = Color(0xFF14120F)
private val InkSurfaceDark = Color(0xFF1E1A16)
private val InkRaisedDark = Color(0xFF28221D)
private val WarmWhiteDark = Color(0xFFF5EFE6)
private val WarmGreyDark = Color(0xFF9C948A)

// Cream + orange light palette.
private val InkBaseLight = Color(0xFFFFF3E0)
private val InkSurfaceLight = Color(0xFFFFFFFF)
private val InkRaisedLight = Color(0xFFF7DCC0)
private val WarmWhiteLight = Color(0xFF2E2018)
private val WarmGreyLight = Color(0xFF7A6455)

val InkBase: Color @Composable get() = if (ThemeController.isDarkTheme) InkBaseDark else InkBaseLight
val InkSurface: Color @Composable get() = if (ThemeController.isDarkTheme) InkSurfaceDark else InkSurfaceLight
val InkRaised: Color @Composable get() = if (ThemeController.isDarkTheme) InkRaisedDark else InkRaisedLight
val WarmWhite: Color @Composable get() = if (ThemeController.isDarkTheme) WarmWhiteDark else WarmWhiteLight
val WarmGrey: Color @Composable get() = if (ThemeController.isDarkTheme) WarmGreyDark else WarmGreyLight

// Brand accents stay constant across both themes.
val Citrus = Color(0xFFE8641C)
val CitrusDim = Color(0xFFB84F17)
val Sage = Color(0xFF7FA650)
val Brick = Color(0xFFC4443A)
