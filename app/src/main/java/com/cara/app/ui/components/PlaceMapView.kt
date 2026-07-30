package com.cara.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private const val DEFAULT_ZOOM = 15f

// Non-interactive by default (scroll/zoom/tilt/rotate all off) - these are
// small inline map thumbnails used for "where is this" context, not a full
// map screen, so gestures fighting with the surrounding scrollable Column
// would be a worse experience than a static preview.
private val THUMBNAIL_UI_SETTINGS = MapUiSettings(
    zoomControlsEnabled = false,
    scrollGesturesEnabled = false,
    zoomGesturesEnabled = false,
    tiltGesturesEnabled = false,
    rotationGesturesEnabled = false,
    myLocationButtonEnabled = false,
    mapToolbarEnabled = false,
)

@Composable
fun PlaceMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val position = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, DEFAULT_ZOOM)
    }

    Surface(shape = RoundedCornerShape(16.dp), modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = THUMBNAIL_UI_SETTINGS,
        ) {
            Marker(state = MarkerState(position = position), title = label)
        }
    }
}
