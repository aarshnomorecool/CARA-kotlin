package com.cara.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Dark is still the default (ThemeController.isDarkTheme starts true), but the
// app now also supports a cream/orange light theme, toggled from Profile.
// The scheme is built inside this composable (not as a top-level val) so its
// color-token args - themselves composable getters in Color.kt - can react to
// ThemeController.isDarkTheme.
@Composable
fun CaraTheme(content: @Composable () -> Unit) {
    val colorScheme = if (ThemeController.isDarkTheme) {
        darkColorScheme(
            primary = Citrus,
            onPrimary = InkBase,
            secondary = Sage,
            onSecondary = InkBase,
            error = Brick,
            onError = WarmWhite,
            background = InkBase,
            onBackground = WarmWhite,
            surface = InkSurface,
            onSurface = WarmWhite,
            surfaceVariant = InkRaised,
            onSurfaceVariant = WarmGrey,
            outline = WarmGrey,
        )
    } else {
        lightColorScheme(
            primary = Citrus,
            onPrimary = InkBase,
            secondary = Sage,
            onSecondary = InkBase,
            error = Brick,
            onError = WarmWhite,
            background = InkBase,
            onBackground = WarmWhite,
            surface = InkSurface,
            onSurface = WarmWhite,
            surfaceVariant = InkRaised,
            onSurfaceVariant = WarmGrey,
            outline = WarmGrey,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CaraTypography,
        content = content,
    )
}
