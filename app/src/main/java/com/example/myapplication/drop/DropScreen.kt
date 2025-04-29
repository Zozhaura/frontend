package com.example.myapplication.drop

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.profile.ProfileViewModel
import com.example.myapplication.utils.TokenManager
import kotlinx.coroutines.delay
import java.lang.Math.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Экран отслеживания потребления воды.
 *
 * Позволяет пользователю фиксировать потребление воды и отображает прогресс относительно дневной нормы.
 * Норма воды рассчитывается на основе веса, пола, возраста и роста пользователя.
 * Требуется API 26 (Android 8.0) и выше из-за использования `java.time`.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DropScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val userResponse by profileViewModel.userResponse
    val errorMessage by profileViewModel.errorMessage
    val currentToken = TokenManager.getToken(context)

    var weight by remember { mutableStateOf<Double?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var height by remember { mutableStateOf<Int?>(null) }
    var lastToken by remember { mutableStateOf(currentToken) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserInfo(context)
    }

    LaunchedEffect(userResponse) {
        userResponse?.let {
            weight = it.weight
            gender = it.gender
            age = it.age
            height = it.height?.toInt()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.contains("Токен отсутствует") || it.contains("Ошибка авторизации")) {
                navController.navigate("login") { popUpTo("drop") { inclusive = true } }
            }
        }
    }

    val maxWater = calculateWaterNorm(weight, gender, age, height)

    val sharedPreferences = remember {
        context.getSharedPreferences("WaterPrefs_${currentToken ?: "default"}", Context.MODE_PRIVATE)
    }

    var currentWater by remember {
        mutableIntStateOf(sharedPreferences.getInt("currentWater", 0))
    }

    var waterPortions by remember {
        mutableStateOf(
            sharedPreferences.getString("waterPortions", "")
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { it.toInt() } ?: emptyList()
        )
    }

    var lastResetDate by remember {
        mutableStateOf(
            sharedPreferences.getString("lastResetDate", null)?.let { LocalDate.parse(it) }
                ?: LocalDate.now()
        )
    }
    fun saveWaterData(
        sharedPreferences: SharedPreferences,
        currentWater: Int,
        waterPortions: List<Int>,
        lastResetDate: LocalDate
    ) {
        sharedPreferences.edit {
            putInt("currentWater", currentWater)
            putString("waterPortions", waterPortions.joinToString(","))
            putString("lastResetDate", lastResetDate.toString())
        }
    }
    LaunchedEffect(currentToken) {
        if (currentToken != lastToken) {
            currentWater = 0
            waterPortions = emptyList()
            lastResetDate = LocalDate.now()
            saveWaterData(sharedPreferences, currentWater, waterPortions, lastResetDate)
            lastToken = currentToken
        }
    }

    fun saveWaterData() {
        saveWaterData(sharedPreferences, currentWater, waterPortions, lastResetDate)
    }

    fun addWaterPortion(amount: Int) {
        currentWater += amount
        waterPortions = listOf(amount) + waterPortions
        saveWaterData()
    }

    fun removeWaterPortion(index: Int) {
        val portionToRemove = waterPortions[index]
        currentWater = (currentWater - portionToRemove).coerceAtLeast(0)
        waterPortions = waterPortions.toMutableList().apply { removeAt(index) }
        saveWaterData()
    }

    fun resetDailyData() {
        currentWater = 0
        waterPortions = emptyList()
        saveWaterData()
    }



    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            if (lastResetDate != today) {
                resetDailyData()
                lastResetDate = today
//                saveWaterData(currentWater, lastResetDate)
            }

            val secondsUntilNextMinute = 60 - now.second
            delay(secondsUntilNextMinute * 1000L)
        }
    }

    val (mainProgress, extraProgress) = if (maxWater > 0) {
        when {
            currentWater <= maxWater -> (currentWater.toFloat() / maxWater to 0f)
            else -> (1f to (currentWater - maxWater).toFloat() / maxWater)
        }
    } else (0f to 0f)

    val animatedMainProgress by animateFloatAsState(
        targetValue = mainProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
    )

    val animatedExtraProgress by animateFloatAsState(
        targetValue = extraProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("drop") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to home screen",
                    tint = Color(0x9AFFFFFF)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF3A3347),
                    strokeWidth = 12.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                )

                CircularProgressIndicator(
                    progress = { animatedMainProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF00BCD4),
                    strokeWidth = 12.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    strokeCap = StrokeCap.Round,
                )
                if (currentWater > maxWater) {
                    CircularProgressIndicator(
                        progress = { animatedExtraProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFFF7043),
                        strokeWidth = 12.dp,
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        strokeCap = StrokeCap.Round,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentWater > 0) {
                        WaterWaveAnimation(
                            progress = minOf(animatedMainProgress + animatedExtraProgress, 1f),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$currentWater мл",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentWater >= maxWater) {
                                "Цель достигнута! 🎉"
                            } else {
                                "Осталось: ${maxWater - currentWater} мл"
                            },
                            color = Color(0x9AFFFFFF),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Норма: $maxWater мл",
                            color = Color(0xFFA0A0A0),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF3A3347),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Выберите объем порции",
                        color = Color(0x9AFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WaterButton(amount = 50, onClick = { addWaterPortion(50) })
                        WaterButton(amount = 100, onClick = { addWaterPortion(100) })
                        WaterButton(amount = 150, onClick = { addWaterPortion(150) })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WaterButton(amount = 200, onClick = { addWaterPortion(200) })
                        WaterButton(amount = 250, onClick = { addWaterPortion(250) })
                        WaterButton(amount = 300, onClick = { addWaterPortion(300) })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { resetDailyData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF6E6E),
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFF6E6E))
                    ) {
                        Text(
                            text = "Сбросить за сегодня",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            if (waterPortions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF494358),
                    border = BorderStroke(1.dp, Color(0xFF6650a4))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Добавленная вода",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            itemsIndexed(waterPortions) { index, portion ->
                                WaterPortionItem(
                                    amount = portion,
                                    onRemove = { removeWaterPortion(index) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
/**
 * Отображает элемент списка добавленных порций воды.
 *
 * @param amount Объем добавленной воды в мл.
 * @param onRemove Callback, вызываемый при удалении порции.
 * @param modifier Modifier для настройки внешнего вида элемента.
 */

@Composable
private fun WaterPortionItem(
    amount: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF5E4D7A)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$amount мл",
                color = Color.White,
                fontSize = 16.sp
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    tint = Color(0xFFFF6E6E)
                )
            }
        }
    }
}

/**
 * Отображает кнопку для добавления объёма выпитой воды.
 *
 * @param text Текст на кнопке (например, "50 мл").
 * @param onClick Callback, вызываемый при нажатии на кнопку.
 */
@Composable
private fun WaterButton(amount: Int, onClick: () -> Unit) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF6650a4),
        contentColor = Color.White
    )
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(60.dp)
            .width(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = buttonColors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF896BDA)
        ),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(
            text = "$amount мл",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Отображает анимацию волн воды внутри кругового индикатора.
 *
 * @param progress Текущий прогресс заполнения (0..1).
 * @param modifier Modifier для настройки внешнего вида анимации.
 */
@Composable
private fun WaterWaveAnimation(progress: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val height = size.height * (1 - progress)
        val waveHeight = 10f
        val waveLength = size.width * 2

        drawRect(
            color = Color(0x3B42A5F5),
            size = Size(size.width, size.height),
            topLeft = Offset(0f, height)
        )

        val path = Path().apply {
            moveTo(0f, height)

            for (x in 0..size.width.toInt() step 10) {
                val y =
                    height + kotlin.math.sin((x.toFloat() / waveLength * 2 * PI).toFloat() + waveOffset * 2 * PI) * waveHeight
                lineTo(x.toFloat(), y.toFloat())
            }

            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = path,
            color = Color(0x5442A5F5)
        )
    }
}

/**
 * Рассчитывает дневную норму потребления воды.
 *
 * Учитывает вес, пол, возраст и рост пользователя. Возвращает значение в миллилитрах.
 *
 * @param weight Вес пользователя в кг.
 * @param gender Пол пользователя ("male" или "female").
 * @param age Возраст пользователя.
 * @param height Рост пользователя в см.
 * @return Норма воды в миллилитрах.
 */
fun calculateWaterNorm(weight: Double?, gender: String?, age: Int?, height: Int?): Int {
    if (weight == null || gender == null || age == null || height == null || weight <= 0 || age <= 0 || height <= 0) {
        return 2000
    }

    val baseWaterPerKg = 30.0
    var calculatedWater = weight * baseWaterPerKg

    val genderFactor = if (gender.lowercase() == "male") 1.1 else 1.0
    calculatedWater *= genderFactor

    val ageFactor = when {
        age < 30 -> 0.95
        age > 60 -> 0.90
        else -> 1.0
    }
    calculatedWater *= ageFactor

    val heightFactor = if (height > 170) 1.05 else if (height < 160) 0.95 else 1.0
    calculatedWater *= heightFactor

    return calculatedWater.toInt().coerceIn(1500, 4000)
}