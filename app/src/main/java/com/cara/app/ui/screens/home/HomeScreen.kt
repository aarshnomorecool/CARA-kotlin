package com.cara.app.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.cara.app.data.remote.PlaceRecommendationDto
import com.cara.app.ui.components.CategoryTiles
import com.cara.app.ui.components.ContextStrip
import com.cara.app.ui.components.RecommendationCard
import com.cara.app.ui.components.SpotlightCarousel
import com.cara.app.ui.components.TILE_CATEGORIES
import com.cara.app.ui.screens.contextinput.ContextInputSheet
import com.cara.app.ui.theme.Brick
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// Nagpur city center - fallback used when location permission is denied or
// unavailable (e.g. an emulator with no mock location configured), so the
// screen still has something sensible to show rather than failing outright.
private const val FALLBACK_LAT = 21.146
private const val FALLBACK_LON = 79.088

// Live-tracking tuning: the system delivers at most one update per
// LOCATION_UPDATE_INTERVAL_MS and only once the device has moved at least
// MIN_UPDATE_DISTANCE_METERS (built-in OS-level debouncing so we're not
// woken up by GPS jitter while stationary). On top of that, a
// /recommendations refetch only fires if the device has moved at least
// REFETCH_DISTANCE_METERS from where the last fetch happened AND at least
// MIN_REFETCH_INTERVAL_MS has passed since - this bounds how often the
// (comparatively slow, especially with a Gemini mood call in flight)
// backend gets hit while still satisfying "moving from college to market
// changes recommendations" without a manual refresh.
private const val LOCATION_UPDATE_INTERVAL_MS = 15_000L
private const val MIN_UPDATE_DISTANCE_METERS = 50f
private const val REFETCH_DISTANCE_METERS = 150f
private const val MIN_REFETCH_INTERVAL_MS = 30_000L

// A brand-new PRIORITY_HIGH_ACCURACY fix (what the live-tracking callback
// below waits for) can take a long time or never arrive at all indoors/with
// weak GPS - if nothing has triggered a fetch by this point, fall back to
// the city center rather than leaving the screen spinning forever.
private const val INITIAL_LOCATION_TIMEOUT_MS = 10_000L

@Composable
fun HomeScreen(
    onPlaceClick: (placeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var deviceLocation by remember { mutableStateOf<Location?>(null) }
    var showContextInput by remember { mutableStateOf(false) }

    // The mood/budget context set via ContextInput persists across
    // location-triggered refreshes (until the user changes it again) rather
    // than resetting to neutral every time the device moves.
    var lastBudget by remember { mutableStateOf<Double?>(null) }
    var lastTextInput by remember { mutableStateOf<String?>(null) }
    var lastFetchLocation by remember { mutableStateOf<Location?>(null) }
    var lastFetchAtMs by remember { mutableStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Gets the screen its first data quickly: a brand-new high-accuracy fix
    // (what the live-tracking effect below waits for) can take a long time
    // or never resolve indoors, so this uses the device's last-known
    // location instead - normally near-instant since it's cached by Play
    // services - and falls back to the city center if neither that nor a
    // live update (below) has produced a fetch within
    // INITIAL_LOCATION_TIMEOUT_MS. Deliberately does NOT touch
    // lastFetchLocation/lastFetchAtMs on the fallback path, so a real fix
    // arriving later still triggers an immediate correction instead of being
    // throttled by MIN_REFETCH_INTERVAL_MS against a fallback that was never
    // a real "fetch location".
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            viewModel.loadRecommendations(FALLBACK_LAT, FALLBACK_LON)
            return@LaunchedEffect
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && lastFetchAtMs == 0L) {
                    deviceLocation = location
                    lastFetchLocation = location
                    lastFetchAtMs = System.currentTimeMillis()
                    viewModel.loadRecommendations(location.latitude, location.longitude, lastBudget, lastTextInput)
                }
            }
        } catch (e: SecurityException) {
            // Permission was revoked between the check above and this call -
            // rare, but possible.
            viewModel.loadRecommendations(FALLBACK_LAT, FALLBACK_LON)
            return@LaunchedEffect
        }

        delay(INITIAL_LOCATION_TIMEOUT_MS)
        if (lastFetchAtMs == 0L) {
            viewModel.loadRecommendations(FALLBACK_LAT, FALLBACK_LON)
        }
    }

    // Continuous live tracking - refetches as the device moves. Also covers
    // the initial load itself whenever it wins the race against the fast
    // path above (e.g. lastLocation returns null on a device with no prior
    // fix), since its own movedEnough/enoughTimePassed checks default to
    // true when lastFetchLocation/lastFetchAtMs are still unset.
    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@DisposableEffect onDispose {}

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                deviceLocation = location

                val movedEnough = lastFetchLocation?.let { it.distanceTo(location) >= REFETCH_DISTANCE_METERS } ?: true
                val enoughTimePassed = System.currentTimeMillis() - lastFetchAtMs >= MIN_REFETCH_INTERVAL_MS
                if (movedEnough && enoughTimePassed) {
                    lastFetchLocation = location
                    lastFetchAtMs = System.currentTimeMillis()
                    viewModel.loadRecommendations(location.latitude, location.longitude, lastBudget, lastTextInput)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // Already handled by the LaunchedEffect above.
        }

        onDispose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    Box(modifier = modifier.fillMaxSize().background(InkBase)) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    color = Citrus,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            is HomeUiState.Error -> {
                Text(
                    "Couldn't load recommendations: ${state.message}",
                    color = WarmGrey,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
            is HomeUiState.Success -> {
                var selectedTile by remember { mutableStateOf<String?>(null) }
                Column(Modifier.fillMaxSize()) {
                    ContextStrip(context = state.data.context)
                    if (state.isOffline) {
                        Surface(color = Brick, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Showing saved results — offline",
                                color = WarmWhite,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                    HomeSections(
                        results = state.data.results,
                        selectedTile = selectedTile,
                        onTileClick = { tile -> selectedTile = if (selectedTile == tile) null else tile },
                        deviceLocation = deviceLocation,
                        onPlaceClick = onPlaceClick,
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showContextInput = true },
            containerColor = Citrus,
            contentColor = InkBase,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Tune, contentDescription = "Adjust mood & budget")
        }
    }

    if (showContextInput) {
        ContextInputSheet(
            onDismiss = { showContextInput = false },
            onSubmit = { textInput, budget ->
                showContextInput = false
                lastBudget = budget
                lastTextInput = textInput
                val loc = deviceLocation
                lastFetchLocation = loc
                lastFetchAtMs = System.currentTimeMillis()
                viewModel.loadRecommendations(
                    lat = loc?.latitude ?: FALLBACK_LAT,
                    lon = loc?.longitude ?: FALLBACK_LON,
                    budget = budget,
                    textInput = textInput,
                )
            },
        )
    }
}

// Home screen body below the Context Strip (2026-07-16 sectioned redesign,
// replacing the old flat top-pick-card + compact-card-list feed): category
// quick-access tiles, a spotlight carousel of the top 3 overall picks
// (always unfiltered - the tiles filter only the curated rows below it,
// not this "here's what the system is most confident about" strip), then
// four curated rows re-sorting the SAME already-fetched list by a different
// score component. No additional API calls - everything here reuses
// `results` from the one /recommendations response already in hand.
@Composable
private fun HomeSections(
    results: List<PlaceRecommendationDto>,
    selectedTile: String?,
    onTileClick: (String) -> Unit,
    deviceLocation: Location?,
    onPlaceClick: (Int) -> Unit,
) {
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No recommendations nearby right now — try again later.",
                color = WarmGrey,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
            )
        }
        return
    }

    val filteredResults = selectedTile?.let { tile ->
        val categories = TILE_CATEGORIES[tile].orEmpty()
        results.filter { it.category in categories }
    } ?: results

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            CategoryTiles(selectedTile = selectedTile, onTileClick = onTileClick)
        }
        item {
            SpotlightCarousel(
                places = results,
                onPlaceClick = onPlaceClick,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
        item {
            CuratedRow(
                title = "Perfect for right now",
                // Already in the model's ranked order - no re-sort needed.
                places = filteredResults,
                deviceLocation = deviceLocation,
                onPlaceClick = onPlaceClick,
            )
        }
        item {
            CuratedRow(
                title = "Low crowd nearby",
                places = filteredResults.sortedBy { it.crowdScore },
                deviceLocation = deviceLocation,
                onPlaceClick = onPlaceClick,
            )
        }
        item {
            CuratedRow(
                title = "Matches your taste",
                places = filteredResults.sortedByDescending { it.preferenceWeight },
                deviceLocation = deviceLocation,
                onPlaceClick = onPlaceClick,
            )
        }
        item {
            CuratedRow(
                title = "Fits your budget",
                places = filteredResults.sortedByDescending { it.budgetFit },
                deviceLocation = deviceLocation,
                onPlaceClick = onPlaceClick,
            )
        }
        item { Spacer(Modifier.height(80.dp)) } // clears the FAB
    }
}

// A single curated row - re-sorts (not re-fetches) the shared results list.
// Hides itself entirely below 3 places, per the redesign spec, rather than
// showing a half-empty/awkward row.
@Composable
private fun CuratedRow(
    title: String,
    places: List<PlaceRecommendationDto>,
    deviceLocation: Location?,
    onPlaceClick: (Int) -> Unit,
) {
    if (places.size < 3) return

    Column(Modifier.padding(bottom = 20.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = WarmWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(places, key = { it.placeId }) { place ->
                RecommendationCard(
                    place = place,
                    distanceMeters = distanceToOrNull(deviceLocation, place),
                    isTopPick = false,
                    onClick = { onPlaceClick(place.placeId) },
                    modifier = Modifier.width(280.dp),
                )
            }
        }
    }
}

private fun distanceToOrNull(deviceLocation: Location?, place: PlaceRecommendationDto): Float? {
    if (deviceLocation == null) return null
    val results = FloatArray(1)
    Location.distanceBetween(deviceLocation.latitude, deviceLocation.longitude, place.latitude, place.longitude, results)
    return results[0]
}
