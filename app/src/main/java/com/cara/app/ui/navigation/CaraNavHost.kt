package com.cara.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cara.app.ui.screens.details.PlaceDetailsScreen
import com.cara.app.ui.screens.home.HomeScreen
import com.cara.app.ui.screens.profile.ProfileScreen
import com.cara.app.ui.screens.saved.SavedScreen

@Composable
fun CaraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = CaraDestination.Home.route,
        modifier = modifier,
    ) {
        composable(CaraDestination.Home.route) {
            HomeScreen(
                onPlaceClick = { placeId ->
                    navController.navigate(CaraDestination.PlaceDetails.createRoute(placeId))
                },
            )
        }
        composable(CaraDestination.Saved.route) {
            SavedScreen(
                onPlaceClick = { placeId ->
                    navController.navigate(CaraDestination.PlaceDetails.createRoute(placeId))
                },
            )
        }
        composable(CaraDestination.Profile.route) {
            ProfileScreen()
        }
        composable(
            route = CaraDestination.PlaceDetails.route,
            arguments = listOf(navArgument("placeId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId") ?: return@composable
            PlaceDetailsScreen(placeId = placeId, onBack = { navController.popBackStack() })
        }
    }
}
