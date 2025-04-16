package com.example.myapplication

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

    val topBarConfig = mapOf(
        "home" to TopBarConfig(title = stringResource(R.string.home_title), showBackButton = false),
        "pulse" to TopBarConfig(
            title = stringResource(R.string.pulse_title),
            showBackButton = true,
            backRoute = "home"
        ),
        "vitamin" to TopBarConfig(
            title = stringResource(R.string.vitamin_title),
            showBackButton = true,
            backRoute = "pulse"
        ),
        "calendar" to TopBarConfig(
            title = stringResource(R.string.calendar_title),
            showBackButton = true,
            backRoute = "home"
        ),
        "sleep" to TopBarConfig(
            title = stringResource(R.string.sleep_title),
            showBackButton = true,
            backRoute = "home"
        )
    )

    Scaffold(
        topBar = {
            val config = topBarConfig[currentRoute] ?: TopBarConfig(
                title = currentRoute?.replaceFirstChar { it.uppercase() } ?: "",
                showBackButton = currentRoute != "registration" && currentRoute != "login"
            )
            TopAppBar(
                title = { Text(config.title) },
                navigationIcon = {
                    if (config.showBackButton) {
                        IconButton(onClick = {
                            if (config.backRoute != null) {
                                navController.navigate(config.backRoute)
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back_button_description)
                            )
                        }
                    }
                }
            )
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
data class TopBarConfig(
    val title: String,
    val showBackButton: Boolean = false,
    val backRoute: String? = null
)