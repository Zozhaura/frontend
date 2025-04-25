package com.example.myapplication

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.navigation.BottomNavigation
import com.example.myapplication.utils.TokenManager

/**
 * Главная точка входа в приложение, управляющая навигацией и отображением UI.
 *
 * Эта функция инициализирует навигацию, определяет стартовый экран на основе состояния пользователя
 * (первый запуск, авторизован или нет) и отображает нижнюю панель навигации для определённых экранов.
 *
 * @requires Android API 26 (Oreo) или выше из-за использования `LocalDate` в зависимых компонентах.
 */
@RequiresApi(Build.VERSION_CODES.O)
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