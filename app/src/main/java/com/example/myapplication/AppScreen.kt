package com.example.myapplication

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.navigation.BottomNavigation

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        topBar = {
            when (currentRoute) {
                "home" -> {
                    TopAppBar(
                        title = { Text("Главная") }
                    )
                }
                "profile", "registration", "drop", "step", "food", "calendar"-> {
                    TopAppBar(
                        title = { Text(currentRoute?.replaceFirstChar { it.uppercase() } ?: "") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (currentRoute == "home") {
                BottomNavigation(navController = navController)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF45364c), Color(0xFF16101B), Color(0xFF2a1f33)),
                        center = androidx.compose.ui.geometry.Offset(0.05f, 0.05f),
                        radius = 1500f
                    )
                )
        ) {
            AppNavigation(navController)
        }
    }
}
