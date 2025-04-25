package com.example.myapplication.food

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.diary.DiaryEntry
import com.example.myapplication.diary.DiaryStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Объект, содержащий цвета, используемые в экране выбора блюд.
 */
object FoodColors {
    val Background = Color(0xFF2E2A3B)
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val TextSecondary = Color(0xFFB0B0B0)
    val ButtonColor = Color(0xFF6A5ACD)
    val SearchBackground = Color(0xFF5E566F)
    val Accent = Color(0xFF00C4B4)
    val CustomDishIndicator = Color(0xFFAB47BC)
}

/**
 * Объект, содержащий размеры элементов интерфейса экрана выбора блюд.
 */
object FoodDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp
    val CardCornerRadius = 16.dp
    val ButtonHeight = 40.dp
    val FoodItemPadding = 16.dp
    val CounterSize = 32.dp
    val SearchHeight = 50.dp
    val ElevationSmall = 4.dp
    val RecipeItemHeight = 100.dp
    val CategoryIndicatorWidth = 4.dp
    val SelectedDishHeight = 80.dp
}

/**
 * Экран выбора блюд для добавления в дневник.
 *
 * Позволяет искать рецепты, фильтровать их, добавлять пользовательские блюда и сохранять выбранные блюда в дневник.
 *
 * @param navController Контроллер навигации.
 * @param viewModel ViewModel для управления данными экрана.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(
    navController: NavController,
    viewModel: FoodViewModel = viewModel()
) {
    val context = LocalContext.current
    val recommendedRecipes by remember { derivedStateOf { viewModel.recommendedRecipes } }
    val selectedDishes by remember { derivedStateOf { viewModel.selectedDishes } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRecipesExpanded by remember { mutableStateOf(true) }
    var isCustomDishExpanded by remember { mutableStateOf(false) }
    var customDishes by remember { mutableStateOf(listOf<CustomDish>()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var includeIngredients by remember { mutableStateOf("") }
    var excludeIngredients by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedMealType by remember { mutableStateOf<String?>(null) }
    val mealTypes = listOf("Завтрак", "Обед", "Ужин", "Перекус")
    val categories = listOf("Салаты", "Вторые блюда", "Первые блюда", "Напитки", "Выпечка", "Десерты")
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun performSearch() {
        val cleanedQuery = searchQuery.trim().ifBlank { null }
        val includes = includeIngredients.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val excludes = excludeIngredients.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val cleanedIncludes = includes.takeIf { it.isNotEmpty() }
        val cleanedExcludes = excludes.takeIf { it.isNotEmpty() }
        Log.d("FoodScreen", "ФИЛЬТРЫ: query=$cleanedQuery, include=$cleanedIncludes, exclude=$cleanedExcludes, category=$selectedCategory")
        isLoading = true
        if (cleanedQuery == null && cleanedIncludes == null && cleanedExcludes == null && selectedCategory == null) {
            viewModel.fetchRecommendedRecipes(context)
        } else {
            viewModel.searchRecipes(
                context = context,
                query = cleanedQuery,
                includeIngredients = cleanedIncludes,
                excludeIngredients = cleanedExcludes,
                category = selectedCategory
            )
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.fetchRecommendedRecipes(context)
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

    val totalNutrition = remember(selectedDishes, customDishes) {
        val fromRecipes = selectedDishes.entries.fold(NutritionDTO(0.0, 0.0, 0.0, 0.0)) { acc, (recipe, weight) ->
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
                calories = acc.calories + dish.calories.toDouble(),
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
                        NutrientTotal("К", totalNutrition.calories.toInt(), "ккал")
                        NutrientTotal("Б", totalNutrition.proteins, "г")
                        NutrientTotal("Ж", totalNutrition.fats, "г")
                        NutrientTotal("У", totalNutrition.carbohydrates, "г")
                    }
                    Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                    Button(
                        onClick = {
                            if (selectedDishes.isEmpty() && customDishes.isEmpty()) {
                                Toast.makeText(context, "Выберите или добавьте блюдо", Toast.LENGTH_SHORT).show()
                            } else if (selectedMealType == null) {
                                Toast.makeText(context, "Укажите тип приема пищи", Toast.LENGTH_SHORT).show()
                            } else {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                selectedDishes.forEach { (recipe, weight) ->
                                    val factor = weight / 100.0
                                    val entry = DiaryEntry(
                                        date = today,
                                        name = recipe.name,
                                        calories = recipe.nutrition.calories * factor,
                                        proteins = recipe.nutrition.proteins * factor,
                                        fats = recipe.nutrition.fats * factor,
                                        carbs = recipe.nutrition.carbohydrates * factor,
                                        mealType = selectedMealType!!,
                                        isCustom = false,
                                        recipeId = recipe.id
                                    )
                                    DiaryStorage.addEntry(context, entry)
                                    Toast.makeText(context, "Добавлено: ${recipe.name} (${weight.toInt()}г, $selectedMealType)", Toast.LENGTH_SHORT).show()
                                }
                                customDishes.forEach { dish ->
                                    val entry = DiaryEntry(
                                        date = today,
                                        name = dish.name,
                                        calories = dish.calories.toDouble(),
                                        proteins = dish.proteins.toDouble(),
                                        fats = dish.fats.toDouble(),
                                        carbs = dish.carbs.toDouble(),
                                        mealType = selectedMealType!!,
                                        isCustom = true,
                                        recipeId = null
                                    )
                                    DiaryStorage.addEntry(context, entry)
                                    Toast.makeText(context, "Добавлено: ${dish.name} (К: ${dish.calories}ккал, Б: ${dish.proteins}г, Ж: ${dish.fats}г, У: ${dish.carbs}г)", Toast.LENGTH_SHORT).show()
                                }
                                viewModel.clearSelectedRecipe()
                                viewModel.clearSelectedDishes()
                                customDishes = emptyList()
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FoodDimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodColors.ButtonColor),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius)
                    ) {
                        Text("Добавить в дневник", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        mealTypes.forEach { type ->
                            Button(
                                onClick = { selectedMealType = type },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .padding(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMealType == type) FoodColors.ButtonColor else FoodColors.SearchBackground
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    color = if (selectedMealType == type) Color.White else FoodColors.TextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FoodColors.Background)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("food") { inclusive = true }
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to home screen",
                        tint = FoodColors.TextColor
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = FoodDimens.PaddingSmall),
                verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue
                            if (newValue.isNotBlank()) {
                                performSearch()
                            } else {
                                isLoading = true
                                viewModel.fetchRecommendedRecipes(context)
                                isLoading = false
                            }
                        },
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
                            Row {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        isLoading = true
                                        viewModel.fetchRecommendedRecipes(context)
                                        isLoading = false
                                    }) {
                                        Icon(Icons.Default.Clear, "Clear", tint = FoodColors.TextColor)
                                    }
                                }
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(Icons.Default.FilterList, "Filters", tint = FoodColors.TextColor)
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
                                        val initialWeight = selectedDishes[item] ?: 0.0
                                        var count by remember { mutableStateOf(initialWeight) }
                                        RecipeItem(
                                            recipe = item,
                                            count = count,
                                            onCountChange = { newCount ->
                                                count = newCount
                                                viewModel.addOrUpdateSelectedDish(item, newCount)
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
                        if (selectedDishes.isNotEmpty() || customDishes.isNotEmpty()) {
                            var isSelectedExpanded by remember { mutableStateOf(true) }
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isSelectedExpanded = !isSelectedExpanded }
                                        .padding(vertical = FoodDimens.PaddingSmall),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Выбранные блюда",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FoodColors.TextColor
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand/Collapse",
                                        tint = FoodColors.TextColor,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(if (isSelectedExpanded) 180f else 0f)
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isSelectedExpanded,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(FoodDimens.PaddingSmall)
                                    ) {
                                        selectedDishes.forEach { (recipe, weight) ->
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
                                                        onClick = { viewModel.addOrUpdateSelectedDish(recipe, 0.0) },
                                                        modifier = Modifier
                                                            .size(FoodDimens.CounterSize)
                                                            .clip(CircleShape)
                                                            .background(FoodColors.SearchBackground)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Clear,
                                                            contentDescription = "Remove",
                                                            tint = FoodColors.TextColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
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
                                                                "К: ${dish.calories}ккал, Б: ${dish.proteins}г, Ж: ${dish.fats}г, У: ${dish.carbs}г",
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
                                                            contentDescription = "Remove",
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
                                var calories by remember { mutableIntStateOf(0) }
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
                                    NutrientCounter("Калории", calories, { if (calories > 0) calories-- }, { calories++ }, "ккал")
                                    NutrientCounter("Белки", protein, { if (protein > 0) protein-- }, { protein++ })
                                }
                                Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NutrientCounter("Жиры", fat, { if (fat > 0) fat-- }, { fat++ })
                                    NutrientCounter("Углеводы", carbs, { if (carbs > 0) carbs-- }, { carbs++ })
                                }
                                Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))
                                Button(
                                    onClick = {
                                        if (name.isBlank()) {
                                            Toast.makeText(context, "Укажите название блюда", Toast.LENGTH_SHORT).show()
                                        } else if (calories == 0 && protein == 0 && fat == 0 && carbs == 0) {
                                            Toast.makeText(context, "Укажите хотя бы одно значение", Toast.LENGTH_SHORT).show()
                                        } else {
                                            customDishes = customDishes + CustomDish(name, calories, protein, fat, carbs)
                                            name = ""
                                            calories = 0
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

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = FoodColors.CardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FoodDimens.PaddingMedium)
                    .padding(bottom = FoodDimens.PaddingLarge)
            ) {
                Text(
                    "Фильтры",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FoodColors.TextColor,
                    modifier = Modifier.padding(bottom = FoodDimens.PaddingMedium)
                )
                var expanded by remember { mutableStateOf(false) }
                Text(
                    "Категория",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = FoodColors.TextColor,
                    modifier = Modifier.padding(bottom = FoodDimens.PaddingSmall)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(FoodColors.SearchBackground, RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = FoodDimens.PaddingSmall),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory ?: "Выберите категорию",
                            fontSize = 14.sp,
                            color = if (selectedCategory != null) Color.White else FoodColors.TextSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = FoodColors.TextColor
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(FoodColors.CardBackground)
                            .fillMaxWidth()
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = FoodColors.TextColor, fontSize = 14.sp) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Очистить", color = FoodColors.TextColor, fontSize = 14.sp) },
                            onClick = {
                                selectedCategory = null
                                expanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(FoodDimens.PaddingMedium))
                OutlinedTextField(
                    value = includeIngredients,
                    onValueChange = { includeIngredients = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Обязательные ингредиенты", color = FoodColors.TextSecondary) },
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
                OutlinedTextField(
                    value = excludeIngredients,
                    onValueChange = { excludeIngredients = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Исключить ингредиенты", color = FoodColors.TextSecondary) },
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
                Spacer(modifier = Modifier.height(FoodDimens.PaddingMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            searchQuery = ""
                            includeIngredients = ""
                            excludeIngredients = ""
                            selectedCategory = null
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showFilterSheet = false
                                isLoading = true
                                viewModel.fetchRecommendedRecipes(context)
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(FoodDimens.ButtonHeight)
                            .padding(end = FoodDimens.PaddingSmall),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodColors.SearchBackground),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius)
                    ) {
                        Text("Сбросить", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FoodColors.TextColor)
                    }
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showFilterSheet = false
                                performSearch()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(FoodDimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodColors.ButtonColor),
                        shape = RoundedCornerShape(FoodDimens.CardCornerRadius)
                    ) {
                        Text("Применить", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Отображает элемент рецепта в списке найденных блюд.
 *
 * @param recipe Объект рецепта.
 * @param count Текущий вес порции.
 * @param onCountChange Callback для изменения веса порции.
 * @param onClick Callback для клика по рецепту.
 */
@Composable
private fun RecipeItem(
    recipe: RecipeShortDTO,
    count: Double,
    onCountChange: (Double) -> Unit,
    onClick: () -> Unit
) {
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
                .padding(FoodDimens.FoodItemPadding)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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

/**
 * Отображает счётчик для значения макронутриента или калорий.
 *
 * @param label Метка (например, "Калории").
 * @param value Текущее значение.
 * @param onDecrease Callback для уменьшения значения.
 * @param onIncrease Callback для увеличения значения.
 * @param unit Единица измерения (по умолчанию "г").
 */
@Composable
private fun NutrientCounter(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    unit: String = "г"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(FoodDimens.CounterSize)
                    .clip(CircleShape)
                    .background(FoodColors.SearchBackground)
            ) { Text("-", fontSize = 18.sp, color = FoodColors.TextColor) }
            Text(
                "$value $unit",
                fontSize = 16.sp,
                color = FoodColors.TextColor,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(FoodDimens.CounterSize)
                    .clip(CircleShape)
                    .background(FoodColors.SearchBackground)
            ) { Icon(Icons.Default.Add, "Increase $label", tint = FoodColors.TextColor) }
        }
        Text(
            label,
            fontSize = 12.sp,
            color = FoodColors.TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Отображает итоговое значение макронутриента или калорий.
 *
 * @param label Метка (например, "К").
 * @param value Значение.
 * @param unit Единица измерения (например, "ккал").
 */
@Composable
private fun NutrientTotal(
    label: String,
    value: Number,
    unit: String
) {
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

/**
 * Модель данных для пользовательского блюда.
 *
 * @param name Название блюда.
 * @param calories Калории.
 * @param proteins Белки.
 * @param fats Жиры.
 * @param carbs Углеводы.
 */
data class CustomDish(val name: String, val calories: Int, val proteins: Int, val fats: Int, val carbs: Int)