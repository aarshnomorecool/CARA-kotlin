package com.cara.app.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cara.app.data.cache.RecommendationCache
import com.cara.app.data.remote.InteractionRequest
import com.cara.app.data.remote.NetworkModule
import com.cara.app.data.session.UserSession
import com.cara.app.ui.components.PlaceMapView
import com.cara.app.ui.components.PlacePhoto
import com.cara.app.ui.components.ReasonChip
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite
import kotlinx.coroutines.launch

private const val PHOTO_HEIGHT_DP = 240
private const val MAP_HEIGHT_DP = 160

// Map provider: Google Maps SDK (confirmed 2026-07-30, see project memory -
// the plumbing for this - play-services-maps dependency, MAPS_API_KEY
// manifest placeholder - already existed unused since the app was scaffolded).

// Core place data always available regardless of how this screen was
// reached; reason/reasonTags are null/empty unless this place came from
// RecommendationCache (i.e. opened from a live /recommendations result on
// Home) - they're relative to the query that produced them, not standalone
// place data, so a place opened from Saved (or any future deep link) simply
// won't have them. See GET /places/{id} in the backend for the fallback path.
private data class PlaceDetailsModel(
    val placeId: Int,
    val name: String,
    val category: String,
    val area: String?,
    val latitude: Double,
    val longitude: Double,
    val approxRating: Double?,
    val priceRange: String?,
    val avgPriceInr: Double?,
    val reason: String?,
    val reasonTags: List<String>,
)

private sealed interface PlaceDetailsUiState {
    data object Loading : PlaceDetailsUiState
    data class Success(val place: PlaceDetailsModel) : PlaceDetailsUiState
    data object NotFound : PlaceDetailsUiState
}

@Composable
fun PlaceDetailsScreen(
    placeId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var uiState by remember { mutableStateOf<PlaceDetailsUiState>(PlaceDetailsUiState.Loading) }
    var isBookmarked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Always resolves by place_id on load, regardless of navigation source.
    // Fast path: RecommendationCache (in-memory, populated by /recommendations
    // - carries the XAI reason/tags). Fallback: GET /places/{id} (works for
    // Saved, deep links, or anywhere RecommendationCache wasn't populated) -
    // this is the fix for "Place not found" when opening a place from Saved,
    // which previously only ever checked the cache and had no fallback.
    LaunchedEffect(placeId) {
        val cached = RecommendationCache.get(placeId)
        val resolved = if (cached != null) {
            PlaceDetailsModel(
                placeId = cached.placeId,
                name = cached.name,
                category = cached.category,
                area = cached.area,
                latitude = cached.latitude,
                longitude = cached.longitude,
                approxRating = cached.approxRating,
                priceRange = cached.priceRange,
                avgPriceInr = cached.avgPriceInr,
                reason = cached.reason,
                reasonTags = cached.reasonTags,
            )
        } else {
            try {
                val response = NetworkModule.apiService.getPlace(placeId)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    PlaceDetailsModel(
                        placeId = body.placeId,
                        name = body.name,
                        category = body.category,
                        area = body.area,
                        latitude = body.latitude,
                        longitude = body.longitude,
                        approxRating = body.approxRating,
                        priceRange = body.priceRange,
                        avgPriceInr = body.avgPriceInr,
                        reason = null,
                        reasonTags = emptyList(),
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        uiState = if (resolved != null) PlaceDetailsUiState.Success(resolved) else PlaceDetailsUiState.NotFound

        if (resolved != null) {
            // Viewing details is itself a mild positive signal, feeding the
            // same EMA preference system bookmarking does.
            postInteraction(resolved.placeId, "click")
            // Check the current saved-places list to initialize the bookmark
            // icon correctly rather than always starting unbookmarked.
            isBookmarked = try {
                val savedResponse = NetworkModule.apiService.getSavedPlaces(UserSession.userId)
                savedResponse.body()?.any { it.placeId == resolved.placeId } ?: false
            } catch (e: Exception) {
                false
            }
        }
    }

    when (val state = uiState) {
        is PlaceDetailsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize().background(InkBase), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Citrus)
            }
        }
        is PlaceDetailsUiState.NotFound -> {
            Box(modifier = modifier.fillMaxSize().background(InkBase), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Place not found", color = WarmWhite, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "This place couldn't be loaded.",
                        color = WarmGrey,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    TextButton(onClick = onBack) { Text("Go back", color = Citrus) }
                }
            }
        }
        is PlaceDetailsUiState.Success -> {
            val place = state.place
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(InkBase)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box {
                    PlacePhoto(
                        placeId = place.placeId,
                        category = place.category,
                        modifier = Modifier.fillMaxWidth().height(PHOTO_HEIGHT_DP.dp),
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(12.dp)
                            .background(InkBase.copy(alpha = 0.55f), shape = CircleShape),
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = WarmWhite)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            place.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = WarmWhite,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            val wasBookmarked = isBookmarked
                            isBookmarked = !isBookmarked
                            scope.launch {
                                if (!wasBookmarked) {
                                    // Logged as an interaction (feeds the EMA
                                    // preference system) - there's no separate
                                    // "unbookmark" interaction type, since removing
                                    // a bookmark isn't a real negative taste signal
                                    // the way a dismiss is, so preference weight is
                                    // left untouched on removal.
                                    postInteraction(place.placeId, "bookmark")
                                } else {
                                    try {
                                        NetworkModule.apiService.deleteSavedPlace(UserSession.userId, place.placeId)
                                    } catch (e: Exception) {
                                        // Best-effort, same as postInteraction - worst
                                        // case it reappears in Saved until retried.
                                    }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                                tint = Citrus,
                            )
                        }
                    }

                    Text(
                        listOfNotNull(
                            place.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                            place.area,
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGrey,
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        place.approxRating?.let {
                            Text("★ ${"%.1f".format(it)}", style = MaterialTheme.typography.titleMedium, color = WarmWhite)
                            Spacer(Modifier.width(16.dp))
                        }
                        place.avgPriceInr?.let {
                            Text("₹${it.toInt()}", style = MaterialTheme.typography.titleMedium, color = WarmWhite)
                        }
                        place.priceRange?.let {
                            Spacer(Modifier.width(8.dp))
                            Text("($it)", style = MaterialTheme.typography.bodyMedium, color = WarmGrey)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    PlaceMapView(
                        latitude = place.latitude,
                        longitude = place.longitude,
                        label = place.name,
                        modifier = Modifier.fillMaxWidth().height(MAP_HEIGHT_DP.dp),
                    )

                    // Only present when this place came from a live
                    // /recommendations result (see PlaceDetailsModel's reason
                    // field doc) - a place opened from Saved has no reason to
                    // show, since it isn't relative to any current query.
                    if (place.reason != null) {
                        Spacer(Modifier.height(20.dp))
                        Text("Why we recommended this", style = MaterialTheme.typography.titleMedium, color = WarmWhite)
                        Spacer(Modifier.height(4.dp))
                        Text(place.reason, style = MaterialTheme.typography.bodyMedium, color = WarmGrey)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            place.reasonTags.forEach { tag -> ReasonChip(text = tag) }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun postInteraction(placeId: Int, action: String) {
    try {
        NetworkModule.apiService.postInteraction(
            InteractionRequest(userId = UserSession.userId, placeId = placeId, action = action)
        )
    } catch (e: Exception) {
        // Best-effort - a failed interaction log shouldn't disrupt the UI.
    }
}
