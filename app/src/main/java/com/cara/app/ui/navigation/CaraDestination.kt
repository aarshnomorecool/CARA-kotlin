package com.cara.app.ui.navigation

// Context Input is a modal bottom sheet launched from Home, not a nav
// destination — see ui/screens/contextinput/ContextInputSheet.kt.
sealed class CaraDestination(val route: String) {
    data object Home : CaraDestination("home")
    data object Saved : CaraDestination("saved")
    data object Profile : CaraDestination("profile")
    data object PlaceDetails : CaraDestination("place/{placeId}") {
        fun createRoute(placeId: Int) = "place/$placeId"
    }
}
