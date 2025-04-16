package com.example.myapplication.diary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object DiaryColors {
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val ProteinColor = Color(0xFF6B5B95)
    val FatColor = Color(0xFFFF6F61)
    val CarbsColor = Color(0xFF88B04B)
    val MealItemColor = Color(0xFF5E4D7A)
    val AddButtonColor = Color(0xFF6B5B95)
}

object DiaryDimens {
    val PaddingMedium = 16.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 4.dp
}

data class Meal(
    val name: String,
    val calories: Int,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val category: String
)

data class DailyNutrition(
    val date: LocalDate,
    val totalCalories: Int,
    val totalProteins: Double,
    val totalFats: Double,
    val totalCarbs: Double,
    val meals: List<Meal>,
    val goals: NutritionGoals = NutritionGoals()
)

data class NutritionGoals(
    val calories: Int = 2000,
    val proteins: Double = 90.0,
    val fats: Double = 70.0,
    val carbs: Double = 275.0
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryScreen(navController: NavController) {
    val dailyNutrition = remember { getSampleData() }

    Column(modifier = Modifier.fillMaxSize()) {
        /*
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DiaryDimens.PaddingMedium),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { /* navController.navigate("addFood") */ },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить еду",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        */

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
                    text = dailyNutrition.date.format(dateFormatter),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DiaryColors.TextColor,
                    modifier = Modifier.padding(bottom = DiaryDimens.PaddingMedium)
                )
            }

            item {
                NutritionSummaryCard(dailyNutrition)
            }

            item {
                MealsList(dailyNutrition.meals)
            }

            item {
                MacronutrientsDistribution(dailyNutrition)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun NutritionSummaryCard(dailyNutrition: DailyNutrition) {
    Surface(
        shape = RoundedCornerShape(DiaryDimens.CardCornerRadius),
        color = DiaryColors.CardBackground,
        shadowElevation = DiaryDimens.CardElevation
    ) {
        Column(modifier = Modifier.padding(DiaryDimens.PaddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionItem("Калории", dailyNutrition.totalCalories, dailyNutrition.goals.calories)
                NutritionItem("Белки", dailyNutrition.totalProteins, dailyNutrition.goals.proteins, "г")
                NutritionItem("Жиры", dailyNutrition.totalFats, dailyNutrition.goals.fats, "г")
                NutritionItem("Углеводы", dailyNutrition.totalCarbs, dailyNutrition.goals.carbs, "г")
            }
        }
    }
}

@Composable
private fun NutritionItem(name: String, current: Number, goal: Number, unit: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = name, color = DiaryColors.TextColor, fontSize = 14.sp)
        Text(text = "$current$unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = "/$goal$unit", color = DiaryColors.TextColor, fontSize = 12.sp)
    }
}

@Composable
private fun MealsList(meals: List<Meal>) {
    Column {
        val categories = listOf("Завтрак", "Обед", "Ужин", "Перекус")

        categories.forEach { category ->
            val categoryMeals = meals.filter { it.category == category }
            MealGroup(
                title = category,
                meals = categoryMeals,
                onAddClick = { /* navController.navigate("addFood?category=$category") */ }
            )
        }
    }
}

@Composable
private fun MealGroup(title: String, meals: List<Meal>, onAddClick: () -> Unit) {
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
                        text = "${meals.sumOf { it.calories }} ккал",
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
                meals.forEach { meal ->
                    MealItem(meal)
                }
            }
        }
    }
}

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
                contentDescription = "Добавить еду",
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

@Composable
private fun MealItem(meal: Meal) {
    var expanded by remember { mutableStateOf(false) }

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
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.name,
                    color = Color.White,
                    modifier = Modifier.weight(1f))

                Text(
                    text = "${meal.calories} ккал",
                    color = DiaryColors.TextColor,
                    fontWeight = FontWeight.Bold
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NutritionInfoItem("Белки", meal.proteins, DiaryColors.ProteinColor)
                    NutritionInfoItem("Жиры", meal.fats, DiaryColors.FatColor)
                    NutritionInfoItem("Углеводы", meal.carbs, DiaryColors.CarbsColor)
                }
            }
        }
    }
}

@Composable
private fun NutritionInfoItem(name: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            color = DiaryColors.TextColor,
            fontSize = 12.sp
        )
        Text(
            text = "${value}г",
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MacronutrientsDistribution(dailyNutrition: DailyNutrition) {
    val total = dailyNutrition.totalProteins + dailyNutrition.totalFats + dailyNutrition.totalCarbs
    val proteinPercent = if (total > 0) dailyNutrition.totalProteins / total else 0.0
    val fatPercent = if (total > 0) dailyNutrition.totalFats / total else 0.0
    val carbsPercent = if (total > 0) dailyNutrition.totalCarbs / total else 0.0

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
                Box(
                    modifier = Modifier
                        .weight(proteinPercent.toFloat())
                        .fillMaxHeight()
                        .background(DiaryColors.ProteinColor)
                )
                Box(
                    modifier = Modifier
                        .weight(fatPercent.toFloat())
                        .fillMaxHeight()
                        .background(DiaryColors.FatColor)
                )
                Box(
                    modifier = Modifier
                        .weight(carbsPercent.toFloat())
                        .fillMaxHeight()
                        .background(DiaryColors.CarbsColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Белки: ${dailyNutrition.totalProteins.toInt()}г", color = DiaryColors.TextColor)
                Text("Жиры: ${dailyNutrition.totalFats.toInt()}г", color = DiaryColors.TextColor)
                Text("Угл: ${dailyNutrition.totalCarbs.toInt()}г", color = DiaryColors.TextColor)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getSampleData(): DailyNutrition {
    return DailyNutrition(
        date = LocalDate.now(),
        totalCalories = 1850,
        totalProteins = 75.0,
        totalFats = 62.0,
        totalCarbs = 245.0,
        meals = listOf(
            Meal("Овсянка", 320, 8.0, 5.0, 60.0, "Завтрак"),
            Meal("Йогурт", 130, 15.0, 3.0, 8.0, "Завтрак"),
            Meal("Куриная грудка", 400, 35.0, 10.0, 30.0, "Обед"),
            Meal("Рис с рыбой", 250, 20.0, 8.0, 25.0, "Обед"),
            Meal("Овощной салат", 350, 12.0, 15.0, 40.0, "Ужин"),
            Meal("Орехи", 200, 6.0, 15.0, 8.0, "Перекус"),
            Meal("Яблоко", 100, 0.5, 0.3, 25.0, "Перекус")
        )
    )
}