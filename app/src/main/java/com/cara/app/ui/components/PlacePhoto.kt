package com.cara.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cara.app.BuildConfig
import com.cara.app.ui.theme.InkRaised
import com.cara.app.ui.theme.WarmGrey

// Shared by RecommendationCard (small, fixed size) and PlaceDetailsScreen
// (full-width banner) - live-fetches from GET /places/{id}/photo (never
// cached client-side beyond Coil's own image cache, per the backend's
// Google Places ToS constraint - see app/routers/places.py).
@Composable
fun PlacePhoto(
    placeId: Int,
    category: String,
    modifier: Modifier = Modifier,
) {
    val photoUrl = "${BuildConfig.BASE_URL}places/$placeId/photo"
    SubcomposeAsyncImage(
        model = photoUrl,
        contentDescription = null,
        // Default is ContentScale.Fit, which letterboxes to preserve each
        // photo's native aspect ratio instead of filling the box - Crop
        // fills the given bounds instead, so photos of different aspect
        // ratios don't look inconsistently sized within a fixed-size card.
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { PlacePhotoLoading() },
        error = { PlacePhotoPlaceholder(category) },
    )
}

@Composable
fun PlacePhotoPlaceholder(category: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InkRaised),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = WarmGrey,
        )
    }
}

@Composable
private fun PlacePhotoLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InkRaised),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = WarmGrey,
            strokeWidth = 2.dp,
            modifier = Modifier.size(20.dp),
        )
    }
}

fun categoryIcon(category: String): ImageVector = when (category) {
    "restaurant" -> Icons.Filled.Restaurant
    "cafe" -> Icons.Filled.LocalCafe
    "park" -> Icons.Filled.Park
    "mall" -> Icons.Filled.LocalMall
    "library" -> Icons.Filled.LocalLibrary
    "gym" -> Icons.Filled.FitnessCenter
    "hospital" -> Icons.Filled.LocalHospital
    "tourist_attraction" -> Icons.Filled.Landscape
    else -> Icons.Filled.Place
}
