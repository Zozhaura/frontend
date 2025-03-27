package com.example.myapplication.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import com.example.myapplication.R

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
    }
}
