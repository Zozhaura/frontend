package com.example.myapplication.step

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate

/**
 * Экран для отображения прогресса шагов и потраченных калорий.
 *
 * Показывает текущий прогресс шагов, позволяет выбрать цель и добавлять шаги вручную.
 *
 * @param navController Контроллер навигации для перехода между экранами.
 * @param viewModel ViewModel для управления данными шагов и калорий.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StepScreen(
    navController: NavController,
    viewModel: StepViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentSteps = viewModel.stepsState.collectAsState()
    val currentCalories = viewModel.caloriesState.collectAsState()
    var maxSteps by remember { mutableStateOf(10000) }
    val currentDate = remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        snapshotFlow { LocalDate.now() }.collect { newDate ->
            if (newDate != currentDate.value) {
                viewModel.resetSteps()
                currentDate.value = newDate
            }
        }
    }

    val progressFirstRing = if (maxSteps > 0) {
        (currentSteps.value.toFloat() / maxSteps).coerceIn(0f, 1f)
    } else {
        0f
    }
    val excessSteps = if (currentSteps.value > maxSteps) currentSteps.value - maxSteps else 0
    val progressSecondRing = if (maxSteps > 0 && currentSteps.value > maxSteps) {
        (excessSteps.toFloat() / maxSteps).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingSteps = if (currentSteps.value < maxSteps) maxSteps - currentSteps.value else 0

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
                        popUpTo("step") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад на главный экран",
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
                    color = Color.LightGray.copy(alpha = 0.3f),
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
                        text = "${currentSteps.value} шагов",
                        color = Color(0x9AFFFFFF),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentSteps.value <= maxSteps) {
                            "$remainingSteps осталось"
                        } else {
                            "Цель выполнена! +$excessSteps шагов"
                        },
                        color = Color(0x9AFFFFFF),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Цель: $maxSteps шагов",
                        color = Color(0x9AFFFFFF),
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                text = "Калорий потрачено: ${currentCalories.value}",
                color = Color(0x9AFFFFFF),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
                        text = "Выберите цель",
                        color = Color(0x9AFFFFFF),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StepButton("4000") { maxSteps = 4000 }
                        StepButton("6000") { maxSteps = 6000 }
                        StepButton("8000") { maxSteps = 8000 }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StepButton("10000") { maxSteps = 10000 }
                        StepButton("12000") { maxSteps = 12000 }
                        StepButton("15000") { maxSteps = 15000 }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    var customGoal by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = customGoal,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                customGoal = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = { Text("Введите свою цель", color = Color(0x9AFFFFFF)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF494358),
                            unfocusedContainerColor = Color(0xFF494358),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFFFFA500),
                            unfocusedIndicatorColor = Color.LightGray,
                            cursorColor = Color(0xFFFFA500)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val goal = customGoal.toIntOrNull()
                            if (goal != null && goal > 0) {
                                maxSteps = goal
                                customGoal = ""
                            } else {
                                Toast.makeText(context, "Введите корректное число", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Установить цель")
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения кнопки выбора цели по шагам.
 *
 * @param text Текст на кнопке.
 * @param onClick Действие, выполняемое при нажатии на кнопку.
 */
@Composable
fun StepButton(text: String, onClick: () -> Unit) {
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