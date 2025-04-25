package com.example.myapplication.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.myapplication.R

/**
 * Нижняя панель навигации приложения.
 *
 * Содержит кнопки для перехода на основные экраны приложения.
 *
 * @param navController Контроллер навигации.
 */
@Composable
fun BottomNavigation(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(painterResource(id = R.drawable.user), contentDescription = "Profile") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("calendar") },
            icon = { Icon(painterResource(id = R.drawable.calendar), contentDescription = "Calendar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("pulse") },
            icon = { Icon(painterResource(id = R.drawable.heart), contentDescription = "Pulse") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("sleep") },
            icon = { Icon(painterResource(id = R.drawable.sleep), contentDescription = "Sleep") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("diary") },
            icon = { Icon(painterResource(id = R.drawable.diary), contentDescription = "Diary") }
        )
    }
}