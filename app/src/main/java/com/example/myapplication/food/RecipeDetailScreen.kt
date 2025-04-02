 package com.example.myapplication.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

 @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: Int,
    viewModel: FoodViewModel
) {
    val context = LocalContext.current
    val selectedRecipe by remember { derivedStateOf { viewModel.selectedRecipe } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    LaunchedEffect(recipeId) {
        viewModel.fetchRecipeDetails(context, recipeId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.contains("Ошибка авторизации") || it.contains("Токен отсутствует")) {
                navController.navigate("login") {
                    popUpTo("food") { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedRecipe?.name ?: "Рецепт",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = FoodColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FoodColors.Background
                )
            )
        },
        modifier = Modifier.background(FoodColors.Background)
    ) { paddingValues ->
        if (selectedRecipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = FoodColors.Accent,
                    strokeWidth = 4.dp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = FoodDimens.PaddingMedium)
                    .padding(bottom = FoodDimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
            ) {
                item {
                    Text(
                        text = selectedRecipe!!.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodColors.TextPrimary
                    )

                    selectedRecipe!!.category?.let {
                        Text(
                            text = "Категория: $it",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = FoodColors.TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(FoodDimens.PaddingMedium))
                }
                selectedRecipe!!.nutrition?.let { nutrition ->
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
                            shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                            color = FoodColors.CardBackground
                        ) {
                            Column(
                                modifier = Modifier.padding(FoodDimens.PaddingMedium),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Пищевая ценность",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FoodColors.TextPrimary
                                )
                                Text(
                                    text = "Калории: ${nutrition.calories.toInt()} ккал",
                                    fontSize = 16.sp,
                                    color = FoodColors.TextSecondary
                                )
                                Text(
                                    text = "Белки: ${nutrition.proteins} г",
                                    fontSize = 16.sp,
                                    color = FoodColors.TextSecondary
                                )
                                Text(
                                    text = "Жиры: ${nutrition.fats} г",
                                    fontSize = 16.sp,
                                    color = FoodColors.TextSecondary
                                )
                                Text(
                                    text = "Углеводы: ${nutrition.carbohydrates} г",
                                    fontSize = 16.sp,
                                    color = FoodColors.TextSecondary
                                )
                                nutrition.dietaryFiber?.let {
                                    Text(
                                        text = "Пищевые волокна: $it г",
                                        fontSize = 16.sp,
                                        color = FoodColors.TextSecondary
                                    )
                                }
                                nutrition.water?.let {
                                    Text(
                                        text = "Вода: $it г",
                                        fontSize = 16.sp,
                                        color = FoodColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                        color = FoodColors.CardBackground
                    ) {
                        Column(
                            modifier = Modifier.padding(FoodDimens.PaddingMedium)
                        ) {
                            Text(
                                text = "Способ приготовления",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FoodColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedRecipe!!.preparationMethod,
                                fontSize = 16.sp,
                                color = FoodColors.TextSecondary,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                        color = FoodColors.CardBackground
                    ) {
                        Column(
                            modifier = Modifier.padding(FoodDimens.PaddingMedium)
                        ) {
                            Text(
                                text = "Ингредиенты",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FoodColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                items(selectedRecipe!!.ingredients) { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = FoodDimens.PaddingMedium),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ingredient.name,
                            fontSize = 16.sp,
                            color = FoodColors.TextSecondary
                        )
                        Text(
                            text = ingredient.quantity,
                            fontSize = 16.sp,
                            color = FoodColors.TextSecondary
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(FoodDimens.PaddingLarge))
                }
            }
        }
    }
}