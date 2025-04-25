package com.example.myapplication.drop

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.myapplication.profile.ProfileViewModel
import kotlinx.coroutines.delay
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

    var weight by remember { mutableStateOf<Double?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var height by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserInfo(context)
    }

    LaunchedEffect(userResponse) {
        userResponse?.let {
            weight = it.weight
            gender = it.gender
            age = it.age
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

    val sharedPreferences = context.getSharedPreferences("WaterPrefs", Context.MODE_PRIVATE)
    var currentWater by remember {
        mutableStateOf(sharedPreferences.getInt("currentWater", 0))
    }

    var lastResetDate by remember {
        mutableStateOf(
            sharedPreferences.getString("lastResetDate", null)?.let { LocalDate.parse(it) }
                ?: LocalDate.now()
        )
    }

    fun saveWaterData(water: Int, resetDate: LocalDate) {
        sharedPreferences.edit {
            putInt("currentWater", water)
            putString("lastResetDate", resetDate.toString())
        }
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            if (lastResetDate != today) {
                currentWater = 0
                lastResetDate = today
                saveWaterData(currentWater, lastResetDate)
            }

            val secondsUntilNextMinute = 60 - now.second
            delay(secondsUntilNextMinute * 1000L)
        }
    }

    LaunchedEffect(currentWater) {
        saveWaterData(currentWater, lastResetDate)
    }

    val progressFirstRing = if (currentWater <= maxWater) {
        (currentWater.toFloat() / maxWater).coerceIn(0f, 1f)
    } else {
        1f
    }

    val excessWater = if (currentWater > maxWater) currentWater - maxWater else 0
    val progressSecondRing = if (currentWater > maxWater) {
        (excessWater.toFloat() / maxWater).coerceIn(0f, 1f)
    } else {
        0f
    }

    val remainingWater = if (currentWater < maxWater) maxWater - currentWater else 0

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
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
                    imageVector = Icons.Default.ArrowBack,
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
                    .size(250.dp)
                    .padding(bottom = 24.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    color = Color(0x90A4A4A4),
                    strokeWidth = 4.dp,
                )

                CircularProgressIndicator(
                    progress = { progressFirstRing },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    color = Color(0xFFFFA500),
                    strokeWidth = 8.dp,
                )

                if (progressSecondRing > 0f) {
                    CircularProgressIndicator(
                        progress = { progressSecondRing },
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        color = Color(0xFF42A5F5),
                        strokeWidth = 8.dp,
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currentWater мл",
                        color = Color(0x9AFFFFFF),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentWater <= maxWater) {
                            "$remainingWater мл осталось"
                        } else {
                            "Норма выполнена! +$excessWater мл"
                        },
                        color = Color(0x9AFFFFFF),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Норма: $maxWater мл",
                        color = Color(0x9AFFFFFF),
                        fontSize = 14.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF494358)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Выберите объем порции",
                        color = Color(0x9AFFFFFF),
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WaterButton("50 мл") {
                            currentWater += 50
                        }
                        WaterButton("100 мл") {
                            currentWater += 100
                        }
                        WaterButton("150 мл") {
                            currentWater += 150
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WaterButton("200 мл") {
                            currentWater += 200
                        }
                        WaterButton("250 мл") {
                            currentWater += 250
                        }
                        WaterButton("300 мл") {
                            currentWater += 300
                        }
                    }
                }
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
fun WaterButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text)
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