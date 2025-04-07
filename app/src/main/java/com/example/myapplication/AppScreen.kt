package com.example.myapplication

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.navigation.BottomNavigation
import com.example.myapplication.utils.TokenManager

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val startDestination = when {
        TokenManager.isFirstLaunch(context) -> "registration"
        TokenManager.getToken(context) != null -> "home"
        else -> "login"
    }

    Scaffold(
        topBar = {
            when (currentRoute) {
                "home" -> {
                }
                "pulse" -> {
                    TopAppBar(
                        title = { Text("Pulse") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate("home") }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
                "vitamin" -> {
                    TopAppBar(
                        title = { Text("Vitamins") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate("pulse") }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
                "calendar" -> {
                    TopAppBar(
                        title = { Text("Calendar") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate("home") }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }

                "sleep" -> {
                    TopAppBar(
                        title = { Text("Sleep") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigate("home") }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }

                else -> {
                    TopAppBar(
                        title = { Text(currentRoute?.replaceFirstChar { it.uppercase() } ?: "") },
                        navigationIcon = {
                            if (currentRoute != "registration" && currentRoute != "login") {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrow_back),
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (currentRoute != "registration" && currentRoute != "login") {
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
            AppNavigation(navController = navController, startDestination = startDestination)
        }
    }
}