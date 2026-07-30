package com.cara.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cara.app.data.remote.SavedPlaceDto
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite

private const val CARD_HEIGHT_DP = 132

// Same compact card footprint as RecommendationCard's non-top-pick variant,
// per CLAUDE_android.md's Saved screen spec - but backed by SavedPlaceDto,
// which has no reason/reason_tags (those don't exist outside a
// recommendation query context), so the reason chip row is replaced with
// the place's area instead.
@Composable
fun SavedPlaceCard(
    place: SavedPlaceDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = InkSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT_DP.dp)
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.fillMaxSize()) {
            PlacePhoto(
                placeId = place.placeId,
                category = place.category,
                modifier = Modifier.fillMaxHeight().width(110.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        place.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    place.approxRating?.let { rating ->
                        Text(
                            "★${"%.1f".format(rating)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmGrey,
                        )
                    }
                }

                Text(
                    listOfNotNull(
                        place.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                        place.area,
                        place.avgPriceInr?.let { "₹${it.toInt()}" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
