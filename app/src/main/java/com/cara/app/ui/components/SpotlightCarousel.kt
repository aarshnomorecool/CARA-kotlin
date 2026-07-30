package com.cara.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cara.app.data.remote.PlaceRecommendationDto
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite

private const val SPOTLIGHT_HEIGHT_DP = 200

// "Recommended right now" - the system's top 3 overall picks, always the
// global top 3 regardless of the category tile filter below it (the tiles
// filter the curated rows further down, not this spotlight - this stays a
// fixed "here's what the system is most confident about" strip). Full-bleed
// HorizontalPager with a peek of the next card, per the 2026-07-16 Home
// screen redesign.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpotlightCarousel(
    places: List<PlaceRecommendationDto>,
    onPlaceClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topThree = places.take(3)
    if (topThree.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { topThree.size })

    Column(modifier) {
        Text(
            "Recommended right now",
            style = MaterialTheme.typography.titleMedium,
            color = WarmWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(SPOTLIGHT_HEIGHT_DP.dp),
        ) { page ->
            val place = topThree[page]
            SpotlightCard(place = place, onClick = { onPlaceClick(place.placeId) })
        }
    }
}

@Composable
private fun SpotlightCard(place: PlaceRecommendationDto, onClick: () -> Unit) {
    Surface(
        color = InkSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize()) {
            PlacePhoto(
                placeId = place.placeId,
                category = place.category,
                modifier = Modifier.fillMaxSize(),
            )
            // Scrim so name/category stay legible over any photo.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, InkBase.copy(alpha = 0.85f)),
                            startY = 60f,
                        )
                    ),
            )
            place.reasonTags.firstOrNull()?.let { topReason ->
                Surface(
                    color = Citrus,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    Text(
                        topReason,
                        color = InkBase,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            ) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = WarmWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    place.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGrey,
                )
            }
        }
    }
}
