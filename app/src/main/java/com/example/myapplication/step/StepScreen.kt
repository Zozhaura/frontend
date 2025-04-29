package com.example.myapplication.step

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate

/**
 * Экран для отображения прогресса шагов и потраченных калорий.
 *
 * Показывает текущий прогресс шагов, позволяет выбрать цель и добавлять шаги вручную.
 * Цель сохраняется в SharedPreferences, чтобы не сбрасываться при выходе.
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
    val sharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    // Загружаем цель из SharedPreferences, по умолчанию 10000
    var maxSteps by remember {
        mutableStateOf(sharedPreferences.getInt("max_steps", 10000))
    }

    // Сохраняем цель при изменении
    LaunchedEffect(maxSteps) {
        sharedPreferences.edit().putInt("max_steps", maxSteps).apply()
    }

    val currentSteps = viewModel.stepsState.collectAsState()
    val currentCalories = viewModel.caloriesState.collectAsState()
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

    val animatedMainProgress by animateFloatAsState(
        targetValue = progressFirstRing,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
    )

    val animatedExtraProgress by animateFloatAsState(
        targetValue = progressSecondRing,
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
                    color = Color(0xFFFFA500),
                    strokeWidth = 12.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    strokeCap = StrokeCap.Round,
                )

                if (currentSteps.value > maxSteps) {
                    CircularProgressIndicator(
                        progress = { animatedExtraProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF42A5F5),
                        strokeWidth = 12.dp,
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        strokeCap = StrokeCap.Round,
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentSteps.value} шагов",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentSteps.value <= maxSteps) {
                            "Осталось: $remainingSteps шагов"
                        } else {
                            "Цель выполнена! +$excessSteps шагов"
                        },
                        color = Color(0x9AFFFFFF),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
                        text = "Выберите цель",
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
                        StepButton("4000", onClick = { maxSteps = 4000 })
                        StepButton("6000", onClick = { maxSteps = 6000 })
                        StepButton("8000", onClick = { maxSteps = 8000 })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StepButton("10000", onClick = { maxSteps = 10000 })
                        StepButton("12000", onClick = { maxSteps = 12000 })
                        StepButton("15000", onClick = { maxSteps = 15000 })
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
                            focusedContainerColor = Color(0xFF3A3347),
                            unfocusedContainerColor = Color(0xFF3A3347),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFFFFA500),
                            unfocusedIndicatorColor = Color(0xFF6650a4),
                            cursorColor = Color(0xFFFFA500)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            .padding(horizontal = 32.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6650a4),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFF896BDA)
                        )
                    ) {
                        Text(
                            text = "Установить цель",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.resetSteps() },
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
private fun StepButton(text: String, onClick: () -> Unit) {
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
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}