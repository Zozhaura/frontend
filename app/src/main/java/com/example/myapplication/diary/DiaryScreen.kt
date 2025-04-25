package com.example.myapplication.diary

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.calculateTargetValues
import com.example.myapplication.profile.ProfileViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Объект, содержащий цвета, используемые в экране дневника.
 */
object DiaryColors {
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val ProteinColor = Color(0xFF6B5B95)
    val FatColor = Color(0xFFFF6F61)
    val CarbsColor = Color(0xFF88B04B)
    val MealItemColor = Color(0xFF5E4D7A)
}

/**
 * Объект, содержащий размеры элементов интерфейса экрана дневника.
 */
object DiaryDimens {
    val PaddingMedium = 16.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 4.dp
}

/**
 * Экран дневника пользователя.
 *
 * Отображает записи питания за текущий день, статистику КБЖУ и распределение макронутриентов.
 * Поддерживает автоматический сброс записей при смене дня. Требуется API 26 (Android 8.0) и выше.
 *
 * @param navController Контроллер навигации.
 * @param profileViewModel ViewModel для получения данных пользователя.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentDate = LocalDate.now().toString()
    var diaryEntries by remember(currentDate) { mutableStateOf(DiaryStorage.getEntriesForDate(context, currentDate)) }

    val sharedPreferences = context.getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
    var lastResetDate by remember {
        mutableStateOf(
            sharedPreferences.getString("lastResetDate", null)?.let { LocalDate.parse(it) }
                ?: LocalDate.now()
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            if (lastResetDate != today) {
                val allEntries = DiaryStorage.loadEntries(context)
                val entriesToKeep = allEntries.filter { it.date == today.toString() }
                DiaryStorage.saveEntries(context, entriesToKeep)
                diaryEntries = entriesToKeep.filter { it.date == currentDate }
                lastResetDate = today
                sharedPreferences.edit {
                    putString("lastResetDate", lastResetDate.toString())
                }
            }

            val secondsUntilNextMinute = 60 - now.second
            delay(secondsUntilNextMinute * 1000L)
        }
    }

    val userResponse by profileViewModel.userResponse
    val errorMessage by profileViewModel.errorMessage

    var height by remember { mutableStateOf<Double?>(null) }
    var weight by remember { mutableStateOf<Double?>(null) }
    var goalWeight by remember { mutableStateOf<Double?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserInfo(context)
    }

    LaunchedEffect(userResponse) {
        userResponse?.let {
            height = it.height
            weight = it.weight
            goalWeight = it.goalWeight
            gender = it.gender
            age = it.age
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.contains("Токен отсутствует") || it.contains("Ошибка авторизации")) {
                navController.navigate("login") {
                    popUpTo("diary") { inclusive = true }
                }
            }
        }
    }

    val totalCalories = diaryEntries.sumOf { it.calories }.toInt()
    val totalProteins = diaryEntries.sumOf { it.proteins }
    val totalFats = diaryEntries.sumOf { it.fats }
    val totalCarbs = diaryEntries.sumOf { it.carbs }

    val targetValues = calculateTargetValues(height, weight, goalWeight, gender, age)
    val proteinTarget = targetValues.first
    val fatTarget = targetValues.second
    val carbsTarget = targetValues.third
    val caloriesTarget = targetValues.fourth

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("diary") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to home screen",
                    tint = DiaryColors.TextColor
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DiaryDimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(DiaryDimens.PaddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    val dateFormatter = remember {
                        DateTimeFormatter.ofPattern("dd MMMM, EEEE", Locale("ru"))
                    }
                    Text(
                        text = LocalDate.now().format(dateFormatter),
                        style = MaterialTheme.typography.headlineSmall,
                        color = DiaryColors.TextColor,
                        modifier = Modifier.padding(bottom = DiaryDimens.PaddingMedium)
                    )
                }

                item {
                    NutritionSummaryCard(
                        totalCalories = totalCalories,
                        totalProteins = totalProteins,
                        totalFats = totalFats,
                        totalCarbs = totalCarbs,
                        targetCalories = caloriesTarget.toInt(),
                        targetProteins = proteinTarget,
                        targetFats = fatTarget,
                        targetCarbs = carbsTarget
                    )
                }

                item {
                    MealsList(diaryEntries, navController)
                }

                item {
                    MacronutrientsDistribution(totalProteins, totalFats, totalCarbs)
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Отображает карточку с общей статистикой КБЖУ за день.
 *
 * @param totalCalories Текущие калории.
 * @param totalProteins Текущие белки.
 * @param totalFats Текущие жиры.
 * @param totalCarbs Текущие углеводы.
 * @param targetCalories Целевые калории.
 * @param targetProteins Целевые белки.
 * @param targetFats Целевые жиры.
 * @param targetCarbs Целевые углеводы.
 */
@Composable
private fun NutritionSummaryCard(
    totalCalories: Int,
    totalProteins: Double,
    totalFats: Double,
    totalCarbs: Double,
    targetCalories: Int,
    targetProteins: Double,
    targetFats: Double,
    targetCarbs: Double
) {
    Surface(
        shape = RoundedCornerShape(DiaryDimens.CardCornerRadius),
        color = DiaryColors.CardBackground,
        shadowElevation = DiaryDimens.CardElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DiaryDimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "КБЖУ за день",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem("Калории", totalCalories.toDouble(), targetCalories.toDouble(), "ккал", Color.White)
                NutritionItem("Белки", totalProteins, targetProteins, "г", DiaryColors.ProteinColor)
                NutritionItem("Жиры", totalFats, targetFats, "г", DiaryColors.FatColor)
                NutritionItem("Углеводы", totalCarbs, targetCarbs, "г", DiaryColors.CarbsColor)
            }
        }
    }
}

/**
 * Отображает один элемент статистики КБЖУ.
 *
 * @param name Название элемента (например, "Калории").
 * @param current Текущее значение.
 * @param goal Целевое значение.
 * @param unit Единица измерения (например, "ккал").
 * @param color Цвет текста.
 */
@Composable
private fun NutritionItem(name: String, current: Double, goal: Double, unit: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = name,
            color = DiaryColors.TextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = "${current.toInt()}",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = unit,
                color = color,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Text(
            text = "/ ${goal.toInt()} $unit",
            color = DiaryColors.TextColor,
            fontSize = 12.sp
        )
    }
}

/**
 * Отображает список групп приёмов пищи (завтрак, обед и т.д.).
 *
 * @param entries Список записей дневника.
 * @param navController Контроллер навигации.
 */
@Composable
private fun MealsList(entries: List<DiaryEntry>, navController: NavController) {
    Column {
        val categories = listOf("Завтрак", "Обед", "Ужин", "Перекус")

        categories.forEach { category ->
            val categoryEntries = entries.filter { it.mealType == category }
            MealGroup(
                title = category,
                entries = categoryEntries,
                onAddClick = { navController.navigate("food") },
                onEntryClick = { entry ->
                    if (!entry.isCustom && entry.recipeId != null) {
                        navController.navigate("recipe_detail/${entry.recipeId}")
                    }
                }
            )
        }
    }
}

/**
 * Отображает группу приёма пищи (например, "Завтрак").
 *
 * @param title Название группы.
 * @param entries Список записей для этой группы.
 * @param onAddClick Callback для добавления нового блюда.
 * @param onEntryClick Callback для клика по записи.
 */
@Composable
private fun MealGroup(
    title: String,
    entries: List<DiaryEntry>,
    onAddClick: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        color = DiaryColors.CardBackground,
        shape = RoundedCornerShape(DiaryDimens.CardCornerRadius),
        shadowElevation = DiaryDimens.CardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entries.sumOf { it.calories }.toInt()} ккал",
                        color = DiaryColors.TextColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                AddMealButton(onClick = onAddClick)
                if (entries.isEmpty()) {
                    Text(
                        text = "Нет блюд",
                        color = DiaryColors.TextColor,
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    entries.forEach { entry ->
                        MealItem(
                            entry = entry,
                            onClick = { onEntryClick(entry) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Отображает кнопку для добавления нового блюда.
 *
 * @param onClick Callback, вызываемый при нажатии на кнопку.
 */
@Composable
private fun AddMealButton(onClick: () -> Unit) {
    Surface(
        color = DiaryColors.MealItemColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add food",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Добавить блюдо",
                color = Color.White
            )
        }
    }
}

/**
 * Отображает элемент блюда в списке приёма пищи.
 *
 * @param entry Запись дневника.
 * @param onClick Callback, вызываемый при клике на элемент.
 */
@Composable
private fun MealItem(
    entry: DiaryEntry,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        color = DiaryColors.MealItemColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!entry.isCustom && entry.recipeId != null) {
                        onClick()
                    } else {
                        expanded = !expanded
                        if (entry.isCustom) {
                            Toast.makeText(context, "Это пользовательское блюдо, подробности недоступны", Toast.LENGTH_SHORT).show()
                        } else if (entry.recipeId == null) {
                            Toast.makeText(context, "ID рецепта отсутствует", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${entry.calories.toInt()} ккал",
                    color = DiaryColors.TextColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expanded || entry.isCustom) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionInfoItem("Белки", entry.proteins, DiaryColors.ProteinColor)
                    NutritionInfoItem("Жиры", entry.fats, DiaryColors.FatColor)
                    NutritionInfoItem("Углеводы", entry.carbs, DiaryColors.CarbsColor)
                }
            }
        }
    }
}

/**
 * Отображает информацию о макронутриенте в записи блюда.
 *
 * @param name Название макронутриента (например, "Белки").
 * @param value Значение макронутриента.
 * @param color Цвет текста.
 */
@Composable
private fun NutritionInfoItem(name: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            color = DiaryColors.TextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Text(
                text = "${value.toInt()}",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "г",
                color = color,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

/**
 * Отображает распределение макронутриентов в виде полоски.
 *
 * @param totalProteins Общее количество белков.
 * @param totalFats Общее количество жиров.
 * @param totalCarbs Общее количество углеводов.
 */
@Composable
private fun MacronutrientsDistribution(
    totalProteins: Double,
    totalFats: Double,
    totalCarbs: Double
) {
    val total = totalProteins + totalFats + totalCarbs
    val proteinPercent = if (total > 0) totalProteins / total else 0.0
    val fatPercent = if (total > 0) totalFats / total else 0.0
    val carbsPercent = if (total > 0) totalCarbs / total else 0.0

    Surface(
        shape = RoundedCornerShape(DiaryDimens.CardCornerRadius),
        color = DiaryColors.CardBackground,
        shadowElevation = DiaryDimens.CardElevation
    ) {
        Column(modifier = Modifier.padding(DiaryDimens.PaddingMedium)) {
            Text(
                text = "Распределение макронутриентов",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            ) {
                if (proteinPercent > 0) {
                    Box(
                        modifier = Modifier
                            .weight(proteinPercent.toFloat())
                            .fillMaxHeight()
                            .background(DiaryColors.ProteinColor)
                    )
                }
                if (fatPercent > 0) {
                    Box(
                        modifier = Modifier
                            .weight(fatPercent.toFloat())
                            .fillMaxHeight()
                            .background(DiaryColors.FatColor)
                    )
                }
                if (carbsPercent > 0) {
                    Box(
                        modifier = Modifier
                            .weight(carbsPercent.toFloat())
                            .fillMaxHeight()
                            .background(DiaryColors.CarbsColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Белки: ${totalProteins.toInt()}г", color = DiaryColors.TextColor)
                Text("Жиры: ${totalFats.toInt()}г", color = DiaryColors.TextColor)
                Text("Угл: ${totalCarbs.toInt()}г", color = DiaryColors.TextColor)
            }
        }
    }
}