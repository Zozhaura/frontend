package com.example.myapplication

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.diary.DiaryStorage
import com.example.myapplication.profile.ProfileViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Объект, содержащий цвета, используемые на главном экране.
 */
object AppColors {
    val Orange = Color(0xFFFFA500)
    val LightGray = Color.LightGray
    val SemiTransparentWhite = Color(0x9AFFFFFF)
    val DarkGray = Color(0xFF3A3A3A)
    val Background = Color(0xFF494358)
    val LoadingColor = Color(0xFF9575CD)
}

/**
 * Объект, содержащий размеры, используемые на главном экране.
 */
object AppDimens {
    val ProgressBarSize = 220.dp
    val MacroProgressBarSize = 110.dp
    val IconSize = 40.dp
    val CardPadding = 8.dp
}

/**
 * Главный экран приложения.
 *
 * Отображает информацию о текущих макронутриентах, прогресс по калориям, ИМТ и предоставляет
 * навигацию к другим экранам.
 *
 * @param navController Контроллер навигации для перехода между экранами.
 * @param viewModel ViewModel для управления данными профиля пользователя.
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val userResponse by viewModel.userResponse
    val errorMessage by viewModel.errorMessage
    val isLoading by viewModel.isLoading
    var height by remember { mutableStateOf<Double?>(null) }
    var weight by remember { mutableStateOf<Double?>(null) }
    var goalWeight by remember { mutableStateOf<Double?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val diaryEntries by remember(currentDate) { mutableStateOf(DiaryStorage.getEntriesForDate(context, currentDate)) }
    val totalCalories = diaryEntries.sumOf { it.calories }
    val totalProteins = diaryEntries.sumOf { it.proteins }
    val totalFats = diaryEntries.sumOf { it.fats }
    val totalCarbs = diaryEntries.sumOf { it.carbs }

    LaunchedEffect(Unit) {
        viewModel.fetchUserInfo(context)
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
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    val targetValues = calculateTargetValues(height, weight, goalWeight, gender, age)
    val proteinTarget = targetValues.first
    val fatTarget = targetValues.second
    val carbsTarget = targetValues.third
    val caloriesTarget = targetValues.fourth

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF45364c), Color(0xFF16101B), Color(0xFF2a1f33)),
                    center = androidx.compose.ui.geometry.Offset(0.05f, 0.05f),
                    radius = 1500f
                )
            )
    ) {
        if (isLoading && userResponse == null) {
            CircularProgressIndicator(
                color = AppColors.LoadingColor,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroInfo(
                        label = "БЕЛКИ",
                        current = totalProteins,
                        target = proteinTarget,
                        progressColor = Color(0xFF6B5B95),
                    )
                    MacroInfo(
                        label = "ЖИРЫ",
                        current = totalFats,
                        target = fatTarget,
                        progressColor = Color(0xFFFF6F61),
                    )
                    MacroInfo(
                        label = "УГЛЕВОДЫ",
                        current = totalCarbs,
                        target = carbsTarget,
                        progressColor = Color(0xFF88B04B),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressCircle(
                        currentCalories = totalCalories,
                        targetCalories = caloriesTarget,
                    )
                    IconButton(
                        iconRes = R.drawable.run,
                        onClick = { navController.navigate("step") },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                    IconButton(
                        iconRes = R.drawable.drop,
                        onClick = { navController.navigate("drop") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.Background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        InfoCard(
                            text = "Текущий вес: ${weight?.toInt() ?: "N/A"} кг",
                            iconRes = R.drawable.pencil,
                            onClick = { navController.navigate("profile") }
                        )
                        InfoCard(
                            text = "Текущий ИМТ: ${calculateBMI(height, weight)}"
                        )
                        InfoCard(
                            text = "Добавить прием пищи",
                            iconRes = R.drawable.ic_add,
                            onClick = { navController.navigate("food") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения информации о макронутриенте (белки, жиры, углеводы).
 *
 * @param label Название макронутриента.
 * @param current Текущее значение макронутриента.
 * @param target Целевое значение макронутриента.
 * @param progressColor Цвет прогресс-бара.
 */
@Composable
fun MacroInfo(
    label: String,
    current: Double,
    target: Double,
    progressColor: Color,
) {
    val progress = if (current <= target) {
        calculateProgress(current, target)
    } else {
        1f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress)
    val excess = if (current > target) current - target else 0.0
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(AppDimens.MacroProgressBarSize)
                .shadow(4.dp, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = AppColors.LightGray,
                strokeWidth = 4.dp
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = progressColor,
                strokeWidth = 8.dp
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AppColors.SemiTransparentWhite,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = if (current <= target) {
                        "${current.toInt()}/${target.toInt()} г"
                    } else {
                        "${current.toInt()} (+${excess.toInt()}) г"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

/**
 * Компонент для отображения прогресса по калориям.
 *
 * @param currentCalories Текущее количество калорий.
 * @param targetCalories Целевое количество калорий.
 */
@Composable
fun ProgressCircle(
    currentCalories: Double,
    targetCalories: Double,
) {
    val progress = if (currentCalories <= targetCalories) {
        calculateProgress(currentCalories, targetCalories)
    } else {
        1f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress)
    val remainingCalories = if (currentCalories < targetCalories) targetCalories - currentCalories else 0.0
    val excessCalories = if (currentCalories > targetCalories) currentCalories - targetCalories else 0.0
    Box(
        modifier = Modifier
            .size(AppDimens.ProgressBarSize)
            .shadow(4.dp, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = AppColors.LightGray,
            strokeWidth = 4.dp
        )
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Orange,
            strokeWidth = 8.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentCalories.toInt()} ккал",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (currentCalories <= targetCalories) {
                    "${remainingCalories.toInt()} ккал осталось"
                } else {
                    "Цель выполнена! +${excessCalories.toInt()} ккал"
                },
                color = AppColors.SemiTransparentWhite,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Цель: ${targetCalories.toInt()} ккал",
                color = AppColors.SemiTransparentWhite,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Компонент для отображения информационной карточки.
 *
 * @param text Текст на карточке.
 * @param iconRes Ресурс иконки (опционально).
 * @param onClick Callback для обработки клика (опционально).
 */
@Composable
fun InfoCard(
    text: String,
    iconRes: Int? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimens.CardPadding),
        shape = RoundedCornerShape(8.dp),
        color = AppColors.DarkGray
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(AppDimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp
            )
            iconRes?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AppColors.SemiTransparentWhite
                )
            }
        }
    }
}

/**
 * Компонент для отображения кнопки с иконкой.
 *
 * @param iconRes Ресурс иконки.
 * @param onClick Callback для обработки клика.
 * @param iconSize Размер иконки.
 * @param modifier Модификатор для компонента.
 */
@Composable
fun IconButton(
    iconRes: Int,
    onClick: () -> Unit,
    iconSize: Dp = AppDimens.IconSize,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = Color.Unspecified
        )
    }
}

/**
 * Вычисляет прогресс для текущего значения относительно целевого.
 *
 * @param current Текущее значение.
 * @param target Целевое значение.
 * @return Прогресс в диапазоне от 0 до 1.
 */
fun calculateProgress(current: Double, target: Double): Float {
    if (target <= 0) return 0f
    return (current / target).toFloat().coerceIn(0f, 1f)
}

/**
 * Вычисляет индекс массы тела (ИМТ).
 *
 * @param height Рост в сантиметрах.
 * @param weight Вес в килограммах.
 * @return Строковое представление ИМТ или "N/A", если данные некорректны.
 */
fun calculateBMI(height: Double?, weight: Double?): String {
    if (height == null || weight == null || height <= 0 || weight <= 0) return "N/A"
    val heightInMeters = height / 100.0
    val bmi = weight / (heightInMeters * heightInMeters)
    val decimalFormat = DecimalFormat("#.#")
    return decimalFormat.format(bmi)
}

/**
 * Вычисляет целевые значения макронутриентов и калорий.
 *
 * @param height Рост в сантиметрах.
 * @param weight Вес в килограммах.
 * @param goalWeight Целевой вес в килограммах.
 * @param gender Пол пользователя ("male" или "female").
 * @param age Возраст пользователя.
 * @return Кортеж из четырёх значений: белки, жиры, углеводы, калории.
 */
fun calculateTargetValues(
    height: Double?,
    weight: Double?,
    goalWeight: Double?,
    gender: String?,
    age: Int?
): Quadruple<Double, Double, Double, Double> {
    if (height == null || weight == null || goalWeight == null || gender == null || age == null ||
        height <= 0 || weight <= 0 || goalWeight <= 0 || age <= 0) {
        return Quadruple(0.0, 0.0, 0.0, 0.0)
    }
    val bmr: Double = if (gender.lowercase() == "male") {
        10 * weight + 6.25 * height - 5 * age + 5
    } else {
        10 * weight + 6.25 * height - 5 * age - 161
    }
    val activityFactor = 1.55
    var totalCalories = bmr * activityFactor
    when {
        goalWeight < weight -> totalCalories *= 0.85
        goalWeight > weight -> totalCalories *= 1.15
    }
    val proteinCalories = totalCalories * 0.30
    val fatCalories = totalCalories * 0.30
    val carbsCalories = totalCalories * 0.40
    val proteinTarget = proteinCalories / 4
    val fatTarget = fatCalories / 9
    val carbsTarget = carbsCalories / 4
    return Quadruple(proteinTarget, fatTarget, carbsTarget, totalCalories)
}

/**
 * Класс данных для хранения четырёх значений.
 *
 * @param first Первое значение.
 * @param second Второе значение.
 * @param third Третье значение.
 * @param fourth Четвёртое значение.
 */
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)