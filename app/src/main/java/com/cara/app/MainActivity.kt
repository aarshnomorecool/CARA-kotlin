package com.cara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cara.app.ui.components.CaraBottomNavBar
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
    Scaffold(
        bottomBar = { CaraBottomNavBar(navController) },
    ) { innerPadding ->
        CaraNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
