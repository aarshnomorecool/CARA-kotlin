package com.cara.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cara.app.data.remote.NetworkModule
import com.cara.app.data.remote.PreferenceDto
import com.cara.app.data.remote.UserDto
import com.cara.app.data.session.UserSession
import com.cara.app.ui.components.PlaceMapView
import com.cara.app.ui.components.categoryIcon
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.InkRaised
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.ThemeController
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite

// Mirrors ml/features.py's CATEGORIES - kept in sync manually since the
// backend doesn't expose this list via an endpoint (PlacePhoto.kt's
// categoryIcon already makes the same "stable, rarely-changing taxonomy"
// assumption). Shown even for categories the user has never interacted
// with, at the cold-start default weight, so the screen reflects the
// user's whole preference profile rather than just the subset they've
// touched - see recommendations.py's DEFAULT_PREFERENCE_WEIGHT.
private val ALL_CATEGORIES = listOf(
    "restaurant", "cafe", "park", "mall", "library", "gym", "hospital", "tourist_attraction",
)
private const val COLD_START_WEIGHT = 1.0 / 8.0

private sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: UserDto, val preferences: List<PreferenceDto>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

@Composable
fun ProfileScreen(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    var uiState by remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }

    LaunchedEffect(Unit) {
        uiState = try {
            val userResponse = NetworkModule.apiService.getUser(UserSession.userId)
            val prefsResponse = NetworkModule.apiService.getPreferences(UserSession.userId)
            val user = userResponse.body()
            val prefs = prefsResponse.body()
            if (userResponse.isSuccessful && user != null && prefsResponse.isSuccessful && prefs != null) {
                ProfileUiState.Success(user, prefs)
            } else {
                ProfileUiState.Error("Couldn't load profile (${userResponse.code()})")
            }
        } catch (e: Exception) {
            ProfileUiState.Error(e.message ?: "Network error")
        }
    }

    Box(modifier = modifier.fillMaxSize().background(InkBase)) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(color = Citrus, modifier = Modifier.align(Alignment.Center))
            }
            is ProfileUiState.Error -> {
                Text(
                    "Couldn't load profile: ${state.message}",
                    color = WarmGrey,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
            is ProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    ProfileHeader(state.user)
                    Spacer(Modifier.height(20.dp))
                    AppearanceCard()
                    Spacer(Modifier.height(20.dp))
                    LocationsCard(state.user)
                    Spacer(Modifier.height(20.dp))
                    PreferencesCard(state.preferences)
                    Spacer(Modifier.height(20.dp))
                    LogoutButton(onLogout)
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: UserDto) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Citrus, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                user.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                color = InkBase,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(user.name, style = MaterialTheme.typography.headlineSmall, color = WarmWhite)
            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = WarmGrey)
        }
    }
}

@Composable
private fun AppearanceCard() {
    Surface(color = InkSurface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium, color = WarmWhite)
                Text(
                    if (ThemeController.isDarkTheme) "Dark theme" else "Light theme",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmGrey,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Switch(
                checked = !ThemeController.isDarkTheme,
                onCheckedChange = { isLight -> ThemeController.isDarkTheme = !isLight },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Citrus,
                    checkedTrackColor = InkRaised,
                    uncheckedThumbColor = WarmGrey,
                    uncheckedTrackColor = InkRaised,
                ),
            )
        }
    }
}

private const val LOCATION_MAP_HEIGHT_DP = 120

@Composable
private fun LocationsCard(user: UserDto) {
    Surface(color = InkSurface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            LocationRow(Icons.Filled.Home, "Home", user.homeLat, user.homeLon)
            Spacer(Modifier.height(14.dp))
            LocationRow(Icons.Filled.Place, "College", user.collegeLat, user.collegeLon)
        }
    }
}

@Composable
private fun LocationRow(icon: ImageVector, label: String, lat: Double?, lon: Double?) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Citrus, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = WarmGrey)
                Text(formatLocation(lat, lon), style = MaterialTheme.typography.bodyMedium, color = WarmWhite)
            }
        }
        if (lat != null && lon != null) {
            Spacer(Modifier.height(8.dp))
            PlaceMapView(
                latitude = lat,
                longitude = lon,
                label = label,
                modifier = Modifier.fillMaxWidth().height(LOCATION_MAP_HEIGHT_DP.dp),
            )
        }
    }
}

@Composable
private fun PreferencesCard(preferences: List<PreferenceDto>) {
    val byCategory = preferences.associateBy { it.category }
    val rows = ALL_CATEGORIES
        .map { category -> category to (byCategory[category]?.weight ?: COLD_START_WEIGHT) }
        .sortedByDescending { it.second }

    Surface(color = InkSurface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Your preferences", style = MaterialTheme.typography.titleMedium, color = WarmWhite)
            Text(
                "Grows from what you click, bookmark, and dismiss — dimmed bars haven't learned anything yet.",
                style = MaterialTheme.typography.labelSmall,
                color = WarmGrey,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
            )
            rows.forEachIndexed { index, (category, weight) ->
                val isLearned = byCategory.containsKey(category)
                PreferenceBar(category = category, weight = weight, isLearned = isLearned)
                if (index != rows.lastIndex) Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PreferenceBar(category: String, weight: Double, isLearned: Boolean) {
    val barColor = if (isLearned) Citrus else WarmGrey
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(categoryIcon(category), contentDescription = null, tint = barColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                category.replace("_", " ").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = WarmWhite,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${(weight * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = WarmGrey,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { weight.toFloat().coerceIn(0f, 1f) },
            color = barColor,
            trackColor = InkRaised,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    OutlinedButton(
        onClick = onLogout,
        border = BorderStroke(1.dp, WarmGrey),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmWhite),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Log out")
    }
}

private fun formatLocation(lat: Double?, lon: Double?): String =
    if (lat != null && lon != null) "%.4f, %.4f".format(lat, lon) else "Not set"
