package com.example.myapplication.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.HomeScreen
import com.example.myapplication.calendar.CalendarScreen
import com.example.myapplication.diary.DiaryScreen
import com.example.myapplication.drop.DropScreen
import com.example.myapplication.food.FoodScreen
import com.example.myapplication.food.FoodViewModel
import com.example.myapplication.food.RecipeDetailScreen
import com.example.myapplication.login.LoginScreen
import com.example.myapplication.profile.ProfileScreen
import com.example.myapplication.pulse.PulseScreen
import com.example.myapplication.registration.RegistrationScreen
import com.example.myapplication.sleep.SleepScreen
import com.example.myapplication.step.StepScreen
import com.example.myapplication.vitamin.VitaminScreen

/**
 * Навигация приложения.
 *
 * Определяет маршруты и соответствующие экраны приложения.
 *
 * @param navController Контроллер навигации.
 * @param startDestination Начальный маршрут навигации.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") { HomeScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("registration") { RegistrationScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("drop") { DropScreen(navController) }
        composable("step") { StepScreen(navController) }
        composable("calendar") { CalendarScreen(navController) }
        composable("pulse") { PulseScreen(navController) }
        composable("vitamin") { VitaminScreen(navController) }
        composable("sleep") { SleepScreen(navController) }
        composable("diary") { DiaryScreen(navController) }
        composable("food") {
            val foodViewModel: FoodViewModel = viewModel()
            FoodScreen(
                navController = navController,
                viewModel = foodViewModel
            )
        }
        composable("recipe_detail/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: 0
            val foodViewModel: FoodViewModel = viewModel()
            RecipeDetailScreen(
                navController = navController,
                recipeId = recipeId,
                viewModel = foodViewModel
            )
        }
    }
}