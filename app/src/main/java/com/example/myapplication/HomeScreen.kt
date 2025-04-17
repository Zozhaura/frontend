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
import com.example.myapplication.profile.ProfileViewModel
import java.text.DecimalFormat

object AppColors {
    val Orange = Color(0xFFFFA500)
    val LightGray = Color.LightGray
    val SemiTransparentWhite = Color(0x9AFFFFFF)
    val DarkGray = Color(0xFF3A3A3A)
    val Background = Color(0xFF494358)
    val LoadingColor = Color(0xFF9575CD)
}

object AppDimens {
    val ProgressBarSize = 200.dp
    val MacroProgressBarSize = 100.dp
    val IconSize = 40.dp
    val CardPadding = 8.dp
}

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
    var protein by remember { mutableStateOf(Pair(0.0, 0.0)) }
    var fat by remember { mutableStateOf(Pair(0.0, 0.0)) }
    var carbs by remember { mutableStateOf(Pair(0.0, 0.0)) }
    var calories by remember { mutableStateOf(Pair(0.0, 0.0)) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserInfo(context)
    }

    LaunchedEffect(userResponse) {
        userResponse?.let {
            height = it.height
            weight = it.weight
            protein = Pair(it.proteinCurrent, it.proteinTarget)
            fat = Pair(it.fatCurrent, it.fatTarget)
            carbs = Pair(it.carbsCurrent, it.carbsTarget)
            calories = Pair(it.caloriesCurrent, it.caloriesTarget)
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
                        current = protein.first,
                        target = protein.second,
                        progressColor = AppColors.Orange,
                    )
                    MacroInfo(
                        label = "ЖИРЫ",
                        current = fat.first,
                        target = fat.second,
                        progressColor = AppColors.Orange,
                    )
                    MacroInfo(
                        label = "УГЛЕВОДЫ",
                        current = carbs.first,
                        target = carbs.second,
                        progressColor = AppColors.Orange,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressCircle(
                        currentCalories = calories.first,
                        targetCalories = calories.second,
                        onClick = { navController.navigate("macros/calories") }
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

@Composable
fun MacroInfo(
    label: String,
    current: Double,
    target: Double,
    progressColor: Color,
) {
    val progress = calculateProgress(current, target)
    val animatedProgress by animateFloatAsState(targetValue = progress)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
    ) {
        Box(
            modifier = Modifier.size(AppDimens.MacroProgressBarSize),
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
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = "${current.toInt()}/${target.toInt()} г",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ProgressCircle(
    currentCalories: Double,
    targetCalories: Double,
    onClick: () -> Unit
) {
    val progress = calculateProgress(currentCalories, targetCalories)
    val animatedProgress by animateFloatAsState(targetValue = progress)

    Box(
        modifier = Modifier
            .size(AppDimens.ProgressBarSize)
            .clickable { onClick() },
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(targetCalories - currentCalories).toInt()} ккал осталось",
                color = AppColors.SemiTransparentWhite,
                fontSize = 16.sp
            )
        }
    }
}

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

fun calculateProgress(current: Double, target: Double): Float {
    if (target <= 0) return 0f
    return (current / target).toFloat().coerceIn(0f, 1f)
}

fun calculateBMI(height: Double?, weight: Double?): String {
    if (height == null || weight == null || height <= 0 || weight <= 0) return "N/A"
    val heightInMeters = height / 100.0
    val bmi = weight / (heightInMeters * heightInMeters)
    val decimalFormat = DecimalFormat("#.#")
    return decimalFormat.format(bmi)
}