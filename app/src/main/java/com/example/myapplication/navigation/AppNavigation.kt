package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.myapplication.HomeScreen
import com.example.myapplication.drop.DropScreen
import com.example.myapplication.food.FoodScreen
import com.example.myapplication.profile.ProfileScreen
import com.example.myapplication.registration.RegistrationScreen
import com.example.myapplication.step.StepScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController) }
        composable("profile") { ProfileScreen() }
        composable("registration") { RegistrationScreen() }
        composable("drop"){ DropScreen() }
        composable("step") { StepScreen() }
        composable("food") { FoodScreen() }
    }
}
