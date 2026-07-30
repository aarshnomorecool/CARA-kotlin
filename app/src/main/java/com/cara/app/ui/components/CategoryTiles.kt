package com.cara.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.WarmWhite

// Home screen's quick-access filter row (2026-07-16 sectioned layout) - maps
// each tile to the `category` values it filters the already-fetched
// recommendation list to. "qsr" isn't a top-level category (it's a
// restaurant sub_category, see ml/features.py's CATEGORIES) so it isn't
// listed separately here - filtering by "restaurant" already includes qsr
// places.
val TILE_CATEGORIES: Map<String, Set<String>> = linkedMapOf(
    "Eat & Drink" to setOf("restaurant", "cafe"),
    "Outdoors" to setOf("park", "tourist_attraction"),
    "Wellness" to setOf("gym", "hospital"),
    "Culture & Work" to setOf("library", "mall"),
)

@Composable
fun CategoryTiles(
    selectedTile: String?,
    onTileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TILE_CATEGORIES.keys.forEach { label ->
            CategoryTile(
                label = label,
                selected = selectedTile == label,
                onClick = { onTileClick(label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CategoryTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) Citrus else InkSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) InkBase else WarmWhite,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
