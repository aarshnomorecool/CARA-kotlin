package com.cara.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// In-memory theme toggle (no persistence layer yet, resets to dark on app
// restart) - mirrors the UserSession singleton pattern already used for the
// hardcoded test user. Color.kt's composable tokens read this to pick a
// light/dark value; ProfileScreen exposes a switch that writes to it.
object ThemeController {
    var isDarkTheme by mutableStateOf(true)
}
