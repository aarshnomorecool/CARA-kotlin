package com.cara.app.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cara.app.data.remote.NetworkModule
import com.cara.app.data.remote.SavedPlaceDto
import com.cara.app.data.session.UserSession
import com.cara.app.ui.components.SavedPlaceCard
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.WarmGrey

private sealed interface SavedUiState {
    data object Loading : SavedUiState
    data class Success(val places: List<SavedPlaceDto>) : SavedUiState
    data class Error(val message: String) : SavedUiState
}

@Composable
fun SavedScreen(
    onPlaceClick: (placeId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var uiState by remember { mutableStateOf<SavedUiState>(SavedUiState.Loading) }

    // Keyed to Unit rather than a ViewModel - re-runs every time this
    // composable re-enters composition (e.g. switching back to the Saved
    // tab), which is what we want so newly bookmarked/unbookmarked places
    // show up without a manual refresh action.
    LaunchedEffect(Unit) {
        uiState = try {
            val response = NetworkModule.apiService.getSavedPlaces(UserSession.userId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                SavedUiState.Success(body)
            } else {
                SavedUiState.Error("Couldn't load saved places (${response.code()})")
            }
        } catch (e: Exception) {
            SavedUiState.Error(e.message ?: "Network error")
        }
    }

    Box(modifier = modifier.fillMaxSize().background(InkBase)) {
        when (val state = uiState) {
            is SavedUiState.Loading -> {
                CircularProgressIndicator(color = Citrus, modifier = Modifier.align(Alignment.Center))
            }
            is SavedUiState.Error -> {
                Text(
                    "Couldn't load saved places: ${state.message}",
                    color = WarmGrey,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
            is SavedUiState.Success -> {
                if (state.places.isEmpty()) {
                    Text(
                        "Nothing saved yet — tap the bookmark on a recommendation to keep it here.",
                        color = WarmGrey,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.places, key = { it.placeId }) { place ->
                            SavedPlaceCard(place = place, onClick = { onPlaceClick(place.placeId) })
                        }
                    }
                }
            }
        }
    }
}
