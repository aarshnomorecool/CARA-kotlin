package com.cara.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkRaised
import com.cara.app.ui.theme.Sage
import com.cara.app.ui.theme.WarmWhite

// Shared between RecommendationCard (capped at 3) and PlaceDetailsScreen
// (shows all of them) - sage-tinted for crowd/eco reasons, citrus-tinted
// for preference matches, per CLAUDE_android.md's XAI chip styling.
@Composable
fun ReasonChip(text: String, modifier: Modifier = Modifier) {
    val accentColor = when {
        "crowd" in text || "eco" in text -> Sage
        "preference" in text -> Citrus
        else -> WarmWhite
    }
    Surface(color = InkRaised, shape = RoundedCornerShape(999.dp), modifier = modifier) {
        Text(
            "⌗ $text",
            color = accentColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
