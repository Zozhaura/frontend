package com.example.myapplication.food

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R

object FoodColors {
    val Background = Color(0xFF2E2A3B)
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val TextPrimary = Color(0xFF673AB7)
    val TextSecondary = Color(0xFFB0B0B0)
    val ButtonColor = Color(0xFF6A5ACD)
    val SearchBackground = Color(0xFF5E566F)
    val Accent = Color(0xFF00C4B4)
    val ProteinIndicator = Color(0xFFEF5350)
    val FatIndicator = Color(0xFFFFCA28)
    val CarbIndicator = Color(0xFF66BB6A)
    val UncategorizedIndicator = Color(0xFF90A4AE)
    val CustomDishIndicator = Color(0xFFAB47BC)
}

object FoodDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp
    val CardCornerRadius = 16.dp
    val ButtonHeight = 56.dp
    val FoodItemPadding = 16.dp
    val CounterSize = 32.dp
    val SearchHeight = 50.dp
    val FoodImageSize = 80.dp
    val ElevationSmall = 4.dp
    val RecipeItemHeight = 100.dp
    val CategoryIndicatorWidth = 4.dp
    val SelectedDishHeight = 80.dp
}

@Composable
fun FoodScreen(
    navController: NavController,
    viewModel: FoodViewModel = viewModel()
) {
    val context = LocalContext.current
    val recommendedRecipes by remember { derivedStateOf { viewModel.recommendedRecipes } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val selectedWeights by remember { derivedStateOf { viewModel.selectedWeights } }
    val selectedMealTypes by remember { derivedStateOf { viewModel.selectedMealTypes } }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRecipesExpanded by remember { mutableStateOf(true) }
    var isCustomDishExpanded by remember { mutableStateOf(false) }
    var customDishes by remember { mutableStateOf(listOf<CustomDish>()) }
    val totalNutrition = remember(selectedWeights, recommendedRecipes, customDishes) {
        val fromRecipes = recommendedRecipes
            .filter { selectedWeights[it.id]?.let { w -> w > 0 } ?: false }
            .fold(NutritionDTO(0.0, 0.0, 0.0, 0.0)) { acc, recipe ->
                val weight = selectedWeights[recipe.id] ?: 0.0
                val factor = weight / 100.0
                NutritionDTO(
                    calories = acc.calories + (recipe.nutrition.calories * factor),
                    proteins = acc.proteins + (recipe.nutrition.proteins * factor),
                    fats = acc.fats + (recipe.nutrition.fats * factor),
                    carbohydrates = acc.carbohydrates + (recipe.nutrition.carbohydrates * factor)
                )
            }
        val fromCustom = customDishes.fold(NutritionDTO(0.0, 0.0, 0.0, 0.0)) { acc, dish ->
            NutritionDTO(
                calories = acc.calories,
                proteins = acc.proteins + dish.proteins.toDouble(),
                fats = acc.fats + dish.fats.toDouble(),
                carbohydrates = acc.carbohydrates + dish.carbs.toDouble()
            )
        }
        NutritionDTO(
            calories = fromRecipes.calories + fromCustom.calories,
            proteins = fromRecipes.proteins + fromCustom.proteins,
            fats = fromRecipes.fats + fromCustom.fats,
            carbohydrates = fromRecipes.carbohydrates + fromCustom.carbohydrates
        )
    }
    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.fetchRecommendedRecipes(context)
        isLoading = false
    }
    LaunchedEffect(searchQuery) {
        isLoading = true
        if (searchQuery.isNotBlank()) viewModel.searchRecipes(context, searchQuery)
        else viewModel.fetchRecommendedRecipes(context)
        isLoading = false
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.contains("Ошибка авторизации") || it.contains("Токен отсутствует")) {
                navController.navigate("login") { popUpTo("food") { inclusive = true } }
            }
        }
    }
    Scaffold(
        modifier = Modifier.background(FoodColors.Background),
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FoodDimens.PaddingMedium)
                    .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
                shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                color = FoodColors.CardBackground
            ) {
                Column(
                    modifier = Modifier.padding(FoodDimens.PaddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Итого:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodColors.TextColor
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Log.d("TotalNutrition", "K: ${totalNutrition.calories}, B: ${totalNutrition.proteins}, J: ${totalNutrition.fats}, U: ${totalNutrition.carbohydrates}")
                        NutrientTotal("К", totalNutrition.calories.toInt(), "ккал")
                        NutrientTotal("Б", totalNutrition.proteins, "г")
                        NutrientTotal("Ж", totalNutrition.fats, "г")
                        NutrientTotal("У", totalNutrition.carbohydrates, "г")
                    }
                    Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                    Button(
                        onClick = {
                            val selectedRecipes = recommendedRecipes.filter { selectedWeights[it.id]?.let { w -> w > 0 } ?: false }
                            if (selectedRecipes.isEmpty() && customDishes.isEmpty()) {
                                Toast.makeText(context, "Выберите или добавьте блюдо", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedRecipes.forEach { recipe ->
                                    val weight = selectedWeights[recipe.id] ?: 0.0
                                    val mealType = selectedMealTypes[recipe.id] ?: "Не указано"
                                    Toast.makeText(context, "Добавлено: ${recipe.name} (${weight.toInt()}г, $mealType)", Toast.LENGTH_SHORT).show()
                                }
                                customDishes.forEach { dish ->
                                    Toast.makeText(context, "Добавлено: ${dish.name} (Б: ${dish.proteins}г, Ж: ${dish.fats}г, У: ${dish.carbs}г)", Toast.LENGTH_SHORT).show()
                                }
                                viewModel.clearSelectedRecipe()
                                customDishes = emptyList()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FoodDimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodColors.ButtonColor),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius)
                    ) {
                        Text("Добавить в дневник", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FoodColors.Background)
                .padding(paddingValues)
                .padding(horizontal = FoodDimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FoodDimens.SearchHeight),
                    shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FoodColors.SearchBackground,
                        unfocusedContainerColor = FoodColors.SearchBackground,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = FoodColors.Accent
                    ),
                    placeholder = { Text("Поиск блюда", color = FoodColors.TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = FoodColors.TextColor) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = FoodColors.TextColor)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isRecipesExpanded = !isRecipesExpanded }
                        .padding(vertical = FoodDimens.PaddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Найденные блюда", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FoodColors.TextColor)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand/Collapse",
                        tint = FoodColors.TextColor,
                        modifier = Modifier.rotate(if (isRecipesExpanded) 180f else 0f)
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isRecipesExpanded, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                        color = FoodColors.CardBackground
                    ) {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(FoodDimens.PaddingLarge), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = FoodColors.Accent, strokeWidth = 4.dp)
                            }
                        } else if (recommendedRecipes.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(FoodDimens.PaddingLarge), contentAlignment = Alignment.Center) {
                                Text("Рецепты не найдены", fontSize = 16.sp, color = FoodColors.TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = FoodDimens.RecipeItemHeight * 5),
                                verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
                            ) {
                                items(recommendedRecipes) { item ->
                                    var count by remember { mutableStateOf(selectedWeights[item.id] ?: 0.0) } // По умолчанию 100 г
                                    RecipeItem(
                                        recipe = item,
                                        count = count,
                                        onCountChange = { newCount ->
                                            count = newCount
                                            viewModel.updateWeight(item.id, newCount)
                                        },
                                        onClick = {
                                            viewModel.clearSelectedRecipe()
                                            navController.navigate("recipe_detail/${item.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Выбранные блюда",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FoodColors.TextColor,
                        modifier = Modifier.padding(bottom = FoodDimens.PaddingSmall)
                    )
                    val selectedRecipes = recommendedRecipes.filter { (selectedWeights[it.id] ?: 0.0) > 0 }
                    Log.d("SelectedDishes", "Selected recipes: ${selectedRecipes.map { "${it.name} (weight: ${selectedWeights[it.id]}, type: ${selectedMealTypes[it.id]})" }}")
                    NutrientCategory("Белки", recommendedRecipes.filter { selectedMealTypes[it.id] == "Белки" && (selectedWeights[it.id] ?: 0.0) > 0 }, selectedWeights, onRemove = { recipe ->
                        viewModel.updateWeight(recipe.id, 0.0)
                    })
                    NutrientCategory("Жиры", recommendedRecipes.filter { selectedMealTypes[it.id] == "Жиры" && (selectedWeights[it.id] ?: 0.0) > 0 }, selectedWeights, onRemove = { recipe ->
                        viewModel.updateWeight(recipe.id, 0.0)
                    })
                    NutrientCategory("Углеводы", recommendedRecipes.filter { selectedMealTypes[it.id] == "Углеводы" && (selectedWeights[it.id] ?: 0.0) > 0 }, selectedWeights, onRemove = { recipe ->
                        viewModel.updateWeight(recipe.id, 0.0)
                    })
                    val uncategorizedRecipes = recommendedRecipes.filter { recipe ->
                        val weight = selectedWeights[recipe.id] ?: 0.0
                        val mealType = selectedMealTypes[recipe.id]
                        weight > 0 && mealType != "Белки" && mealType != "Жиры" && mealType != "Углеводы"
                    }
                    NutrientCategory("Без категории", uncategorizedRecipes, selectedWeights, onRemove = { recipe ->
                        viewModel.updateWeight(recipe.id, 0.0)
                    })
                    if (customDishes.isNotEmpty()) {
                        var isCustomExpanded by remember { mutableStateOf(true) }
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCustomExpanded = !isCustomExpanded }
                                    .padding(vertical = FoodDimens.PaddingSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Добавленные вручную",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FoodColors.TextColor
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand/Collapse",
                                    tint = FoodColors.TextColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .rotate(if (isCustomExpanded) 180f else 0f)
                                )
                            }

                            AnimatedVisibility(
                                visible = isCustomExpanded,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
                                ) {
                                    customDishes.forEachIndexed { index, dish ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(FoodDimens.SelectedDishHeight)
                                                .shadow(
                                                    FoodDimens.ElevationSmall,
                                                    RoundedCornerShape(FoodDimens.CardCornerRadius)
                                                ),
                                            shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                                            color = FoodColors.CardBackground
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(FoodDimens.FoodItemPadding),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(FoodDimens.CategoryIndicatorWidth)
                                                        .fillMaxHeight()
                                                        .background(FoodColors.CustomDishIndicator)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = FoodDimens.PaddingMedium)
                                                ) {
                                                    Column {
                                                        Text(
                                                            dish.name,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = FoodColors.TextColor,
                                                            maxLines = 1
                                                        )
                                                        Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                                                        Text(
                                                            "Б: ${dish.proteins}г, Ж: ${dish.fats}г, У: ${dish.carbs}г",
                                                            fontSize = 14.sp,
                                                            color = FoodColors.TextSecondary
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = {
                                                        customDishes = customDishes.toMutableList().apply { removeAt(index) }
                                                    },
                                                    modifier = Modifier
                                                        .size(FoodDimens.CounterSize)
                                                        .clip(CircleShape)
                                                        .background(FoodColors.SearchBackground)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Удалить",
                                                        tint = FoodColors.TextColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCustomDishExpanded = !isCustomDishExpanded }
                        .padding(vertical = FoodDimens.PaddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Добавить своё блюдо", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FoodColors.TextColor)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand/Collapse",
                        tint = FoodColors.TextColor,
                        modifier = Modifier.rotate(if (isCustomDishExpanded) 180f else 0f)
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isCustomDishExpanded, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                        color = FoodColors.CardBackground
                    ) {
                        Column(modifier = Modifier.padding(FoodDimens.PaddingMedium)) {
                            var name by remember { mutableStateOf("") }
                            var protein by remember { mutableIntStateOf(0) }
                            var fat by remember { mutableIntStateOf(0) }
                            var carbs by remember { mutableIntStateOf(0) }
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Название блюда", color = FoodColors.TextSecondary) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = FoodColors.SearchBackground,
                                    unfocusedContainerColor = FoodColors.SearchBackground,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = FoodColors.Accent
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NutrientCounter("Белки", protein, { if (protein > 0) protein-- }, { protein++ })
                                NutrientCounter("Жиры", fat, { if (fat > 0) fat-- }, { fat++ })
                                NutrientCounter("Углеводы", carbs, { if (carbs > 0) carbs-- }, { carbs++ })
                            }
                            Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                            Button(
                                onClick = {
                                    if (name.isBlank()) {
                                        Toast.makeText(context, "Укажите название блюда", Toast.LENGTH_SHORT).show()
                                    } else if (protein == 0 && fat == 0 && carbs == 0) {
                                        Toast.makeText(context, "Укажите хотя бы одно значение КБЖУ", Toast.LENGTH_SHORT).show()
                                    } else {
                                        customDishes = customDishes + CustomDish(name, protein, fat, carbs)
                                        name = ""
                                        protein = 0
                                        fat = 0
                                        carbs = 0
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(FoodDimens.ButtonHeight),
                                colors = ButtonDefaults.buttonColors(containerColor = FoodColors.ButtonColor),
                                shape = RoundedCornerShape(FoodDimens.CardCornerRadius)
                            ) {
                                Text("Добавить блюдо", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(FoodDimens.PaddingLarge)) }
        }
    }
}
@Composable
private fun RecipeItem(
    recipe: RecipeShortDTO,
    count: Double,
    onCountChange: (Double) -> Unit,
    onClick: () -> Unit
) {
    Log.d("RecipeItem", "Recipe: ${recipe.name}, K: ${recipe.nutrition.calories}, B: ${recipe.nutrition.proteins}, J: ${recipe.nutrition.fats}, U: ${recipe.nutrition.carbohydrates}, Weight: $count")
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(FoodDimens.RecipeItemHeight)
            .shadow(FoodDimens.ElevationSmall, RoundedCornerShape(FoodDimens.CardCornerRadius)),
        shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
        color = FoodColors.CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(FoodDimens.FoodItemPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.shrek),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .size(FoodDimens.FoodImageSize)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FoodColors.SearchBackground)
                )
                Spacer(modifier = Modifier.width(FoodDimens.PaddingMedium))
                Column {
                    Text(recipe.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FoodColors.TextColor, maxLines = 2)
                    Text("${recipe.nutrition.calories.toInt()} ккал", fontSize = 14.sp, color = FoodColors.TextSecondary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { if (count > 0) onCountChange(count - 10) },
                    modifier = Modifier.size(FoodDimens.CounterSize).clip(CircleShape).background(FoodColors.SearchBackground)
                ) { Text("-", fontSize = 18.sp, color = FoodColors.TextColor) }
                Text("${count.toInt()} г", fontSize = 16.sp, color = FoodColors.TextColor, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(
                    onClick = { onCountChange(count + 10) },
                    modifier = Modifier.size(FoodDimens.CounterSize).clip(CircleShape).background(FoodColors.SearchBackground)
                ) { Icon(Icons.Default.Add, "Increase", tint = FoodColors.TextColor) }
            }
        }
    }
}
@Composable
private fun NutrientCounter(label: String, value: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(FoodDimens.CounterSize).clip(CircleShape).background(FoodColors.SearchBackground)
            ) { Text("-", fontSize = 18.sp, color = FoodColors.TextColor) }
            Text("$value г", fontSize = 16.sp, color = FoodColors.TextColor, modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(FoodDimens.CounterSize).clip(CircleShape).background(FoodColors.SearchBackground)
            ) { Icon(Icons.Default.Add, "Increase $label", tint = FoodColors.TextColor) }
        }
        Text(label, fontSize = 12.sp, color = FoodColors.TextSecondary, modifier = Modifier.padding(top = 4.dp))
    }
}
@Composable
private fun NutrientCategory(
    title: String,
    recipes: List<RecipeShortDTO>,
    selectedWeights: Map<Int, Double>,
    onRemove: (RecipeShortDTO) -> Unit
) {
    if (recipes.isNotEmpty()) {
        var isExpanded by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FoodDimens.PaddingSmall)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = FoodDimens.PaddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FoodColors.TextColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand/Collapse",
                    tint = FoodColors.TextColor,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isExpanded) 180f else 0f)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
                ) {
                    recipes.forEach { recipe ->
                        val weight = selectedWeights[recipe.id] ?: 0.0
                        val proteins = (recipe.nutrition.proteins * (weight / 100.0))
                        val fats = (recipe.nutrition.fats * (weight / 100.0))
                        val carbs = (recipe.nutrition.carbohydrates * (weight / 100.0))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(FoodDimens.SelectedDishHeight)
                                .shadow(
                                    FoodDimens.ElevationSmall,
                                    RoundedCornerShape(FoodDimens.CardCornerRadius)
                                ),
                            shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                            color = FoodColors.CardBackground
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(FoodDimens.FoodItemPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(FoodDimens.CategoryIndicatorWidth)
                                        .fillMaxHeight()
                                        .background(
                                            when (title) {
                                                "Белки" -> FoodColors.ProteinIndicator
                                                "Жиры" -> FoodColors.FatIndicator
                                                "Углеводы" -> FoodColors.CarbIndicator
                                                else -> FoodColors.UncategorizedIndicator
                                            }
                                        )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = FoodDimens.PaddingMedium)
                                ) {
                                    Column {
                                        Text(
                                            "${recipe.name} (${weight.toInt()} г)",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = FoodColors.TextColor,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                                        Text(
                                            "Б: ${String.format("%.1f", proteins)}г, Ж: ${String.format("%.1f", fats)}г, У: ${String.format("%.1f", carbs)}г",
                                            fontSize = 14.sp,
                                            color = FoodColors.TextSecondary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onRemove(recipe) },
                                    modifier = Modifier
                                        .size(FoodDimens.CounterSize)
                                        .clip(CircleShape)
                                        .background(FoodColors.SearchBackground)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Удалить",
                                        tint = FoodColors.TextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientTotal(label: String, value: Number, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = FoodColors.TextColor)
        Text(
            text = if (value is Int) "$value $unit" else "${String.format("%.1f", value)} $unit",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = FoodColors.Accent
        )
    }
}

data class CustomDish(val name: String, val proteins: Int, val fats: Int, val carbs: Int)