package com.example.myapplication.diary

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val ProteinColor = Color(0xFF7841FF)
    val FatColor = Color(0xFFFF7043)
    val CarbsColor = Color(0xFF8BFF00)
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
    val totalCalories = entries.sumOf { it.calories }.toInt()

    Surface(
        color = Color(0xFF3A3347),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MealTypeIcon(mealType = title)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$totalCalories ккал",
                            color = DiaryColors.TextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (expanded) {
                AddMealButton(onClick = onAddClick)
                if (entries.isEmpty()) {
                    Text(
                        text = "Нет блюд",
                        color = DiaryColors.TextColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        entries.forEach { entry ->
                            ModernMealItem(
                                entry = entry,
                                onClick = { onEntryClick(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTypeIcon(mealType: String) {
    val icon = when(mealType) {
        "Завтрак" -> Icons.Default.WbSunny
        "Обед" -> Icons.Default.LunchDining
        "Ужин" -> Icons.Default.DinnerDining
        else -> Icons.Default.LocalCafe
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = when(mealType) {
                    "Завтрак" -> Color(0xFFFFA726).copy(alpha = 0.2f)
                    "Обед" -> Color(0xFF66BB6A).copy(alpha = 0.2f)
                    "Ужин" -> Color(0xFF42A5F5).copy(alpha = 0.2f)
                    else -> Color(0xFFAB47BC).copy(alpha = 0.2f)
                },
                shape = CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = mealType,
            tint = when(mealType) {
                "Завтрак" -> Color(0xFFFFA726)
                "Обед" -> Color(0xFF66BB6A)
                "Ужин" -> Color(0xFF42A5F5)
                else -> Color(0xFFAB47BC)
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Отображает кнопку для добавления нового блюда.
 *
 * @param onClick Callback, вызываемый при нажатии на кнопку.
 */
@Composable
private fun AddMealButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFF5722)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFFFF5722))
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add food",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Добавить блюдо",
            fontSize = 16.sp
        )
    }
}

/**
 * Отображает элемент блюда в списке приёма пищи.
 *
 * @param entry Запись дневника.
 * @param onClick Callback, вызываемый при клике на элемент.
 */
@Composable
private fun ModernMealItem(
    entry: DiaryEntry,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
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
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A4458))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${entry.calories.toInt()} ккал",
                    color = DiaryColors.TextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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
 * Отображает распределение макронутриентов в виде круговой диаграммы с детализацией.
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
    val proteinPercent = if (total > 0) (totalProteins / total * 100).toFloat() else 0f
    val fatPercent = if (total > 0) (totalFats / total * 100).toFloat() else 0f
    val carbsPercent = if (total > 0) (totalCarbs / total * 100).toFloat() else 0f

    Surface(
        shape = RoundedCornerShape(DiaryDimens.CardCornerRadius),
        color = DiaryColors.CardBackground,
        shadowElevation = DiaryDimens.CardElevation,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(DiaryDimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Баланс макронутриентов",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    PieChart(
                        proteinPercent = proteinPercent,
                        fatPercent = fatPercent,
                        carbsPercent = carbsPercent,
                        modifier = Modifier.size(120.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutrientLegendItem(
                        color = DiaryColors.ProteinColor,
                        name = "Белки",
                        value = totalProteins,
                    )

                    NutrientLegendItem(
                        color = DiaryColors.FatColor,
                        name = "Жиры",
                        value = totalFats,
                    )

                    NutrientLegendItem(
                        color = DiaryColors.CarbsColor,
                        name = "Углеводы",
                        value = totalCarbs,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientDetailItem(
                    value = totalProteins.toInt(),
                    unit = "г",
                    label = "Белки",
                    color = DiaryColors.ProteinColor
                )

                NutrientDetailItem(
                    value = totalFats.toInt(),
                    unit = "г",
                    label = "Жиры",
                    color = DiaryColors.FatColor
                )

                NutrientDetailItem(
                    value = totalCarbs.toInt(),
                    unit = "г",
                    label = "Углеводы",
                    color = DiaryColors.CarbsColor
                )

                NutrientDetailItem(
                    value = total.toInt(),
                    unit = "г",
                    label = "Всего",
                    color = DiaryColors.TextColor
                )
            }
        }
    }
}

/**
 * Круговая диаграмма распределения макронутриентов.
 */
@Composable
private fun PieChart(
    proteinPercent: Float,
    fatPercent: Float,
    carbsPercent: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokeWidth = diameter * 0.15f

        var startAngle = -90f

        drawArc(
            color = DiaryColors.ProteinColor,
            startAngle = startAngle,
            sweepAngle = proteinPercent * 3.6f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(diameter - strokeWidth, diameter - strokeWidth),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += proteinPercent * 3.6f

        drawArc(
            color = DiaryColors.FatColor,
            startAngle = startAngle,
            sweepAngle = fatPercent * 3.6f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(diameter - strokeWidth, diameter - strokeWidth),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        startAngle += fatPercent * 3.6f

        drawArc(
            color = DiaryColors.CarbsColor,
            startAngle = startAngle,
            sweepAngle = carbsPercent * 3.6f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(diameter - strokeWidth, diameter - strokeWidth),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Элемент легенды для макронутриента.
 */
@Composable
private fun NutrientLegendItem(
    color: Color,
    name: String,
    value: Double,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$name: ${value.toInt()}г",
            color = DiaryColors.TextColor,
            fontSize = 14.sp
        )
    }
}

/**
 * Компактный элемент с информацией о нутриенте (значение и единицы на одной строке).
 */
@Composable
private fun NutrientDetailItem(
    value: Int,
    unit: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$value",
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                color = color,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(
            text = label,
            color = DiaryColors.TextColor,
            fontSize = 12.sp
        )
    }
}