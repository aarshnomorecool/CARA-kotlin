package com.cara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cara.app.ui.components.CaraBottomNavBar
import com.cara.app.ui.navigation.CaraDestination
import com.cara.app.ui.navigation.CaraNavHost
import com.cara.app.ui.theme.CaraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaraTheme {
                CaraApp()
            }
        }
    }
}

@Composable
private fun CaraApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // No bottom nav on Login - it's not one of the 3 tabbed destinations,
    // and showing it there would let an unauthenticated user tap straight
    // into Home/Saved/Profile past the login gate.
    val showBottomBar = backStackEntry?.destination?.route != CaraDestination.Login.route

    Scaffold(
        bottomBar = { if (showBottomBar) CaraBottomNavBar(navController) },
    ) { innerPadding ->
        CaraNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
