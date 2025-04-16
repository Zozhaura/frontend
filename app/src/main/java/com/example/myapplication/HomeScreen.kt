package com.example.myapplication

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val userResponse by remember { derivedStateOf { viewModel.userResponse } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }

    var height by remember { mutableStateOf(0) }
    var weight by remember { mutableStateOf(0) }

    val protein by remember { mutableStateOf("45/90 г") }
    val fat by remember { mutableStateOf("24/48 г") }
    val carbs by remember { mutableStateOf("62/125 г") }
    val calories by remember { mutableStateOf("1300 ккал") }
    val caloriesLeft by remember { mutableStateOf("800 ккал осталось") }

    LaunchedEffect(Unit) {
        viewModel.fetchUserInfo(context)
    }

    LaunchedEffect(userResponse) {
        userResponse?.let {
            weight = it.weight.toInt()
            height = it.height.toInt()
        }
    }

    val bmi = calculateBMI(height, weight)

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
            MacroInfo("БЕЛКИ", protein, progressColor = AppColors.Orange)
            MacroInfo("ЖИРЫ", fat, progressColor = AppColors.Orange)
            MacroInfo("УГЛЕВОДЫ", carbs, progressColor = AppColors.Orange)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            ProgressCircle(calories, caloriesLeft, calculateProgress(calories))
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
                InfoCard("Текущий вес: $weight кг", R.drawable.pencil)
                InfoCard("Текущий ИМТ: $bmi")
                InfoCard("Добавить прием пищи", R.drawable.ic_add) {
                    navController.navigate("food")
                }
            }
        }
    }
}

@Composable
fun MacroInfo(label: String, value: String, progressColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier.size(AppDimens.MacroProgressBarSize),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = AppColors.LightGray,
                strokeWidth = 4.dp,
            )
            CircularProgressIndicator(
                progress = { calculateProgress(value) },
                modifier = Modifier.fillMaxSize(),
                color = progressColor,
                strokeWidth = 8.dp,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.SemiTransparentWhite)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(color = AppColors.SemiTransparentWhite)
                )
            }
        }
    }
}

@Composable
fun ProgressCircle(calories: String, caloriesLeft: String, progress: Float) {
    Box(
        modifier = Modifier.size(AppDimens.ProgressBarSize),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = AppColors.LightGray,
            strokeWidth = 4.dp,
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = AppColors.Orange,
            strokeWidth = 8.dp,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = calories,
                color = AppColors.SemiTransparentWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = caloriesLeft,
                color = AppColors.SemiTransparentWhite,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun InfoCard(text: String, iconRes: Int? = null, onClick: (() -> Unit)? = null) {
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
                color = AppColors.SemiTransparentWhite,
                fontSize = 18.sp
            )
            iconRes?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun IconButton(iconRes: Int, onClick: () -> Unit, iconSize: Dp = AppDimens.IconSize, modifier: Modifier = Modifier) {
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

fun calculateProgress(value: String): Float {
    val values = value.replace(Regex("[^\\d/]"), "").split("/")
    val currentValue = values[0].toFloatOrNull() ?: 0f
    val totalValue = values.getOrNull(1)?.toFloatOrNull() ?: 1f
    return (currentValue / totalValue).coerceIn(0f, 1f)
}

fun calculateBMI(height: Int, weight: Int): String {
    if (height <= 0 || weight <= 0) return "N/A"
    val heightInMeters = height / 100.0
    val bmi = weight / (heightInMeters * heightInMeters)
    val decimalFormat = DecimalFormat("#.#")
    return decimalFormat.format(bmi)
}