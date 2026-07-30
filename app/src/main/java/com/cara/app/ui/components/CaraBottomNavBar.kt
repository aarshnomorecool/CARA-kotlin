package com.cara.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cara.app.ui.navigation.CaraDestination
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.WarmGrey

private data class BottomNavItem(
    val destination: CaraDestination,
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(CaraDestination.Home, "Home", Icons.Filled.Home),
    BottomNavItem(CaraDestination.Saved, "Saved", Icons.Filled.Bookmark),
    BottomNavItem(CaraDestination.Profile, "Profile", Icons.Filled.Person),
)

@Composable
fun CaraBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = InkSurface) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.destination.route,
                onClick = {
                    navController.navigate(item.destination.route) {
                        popUpTo(CaraDestination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Citrus,
                    selectedTextColor = Citrus,
                    unselectedIconColor = WarmGrey,
                    unselectedTextColor = WarmGrey,
                    indicatorColor = InkSurface,
                ),
            )
        }
    }
}
