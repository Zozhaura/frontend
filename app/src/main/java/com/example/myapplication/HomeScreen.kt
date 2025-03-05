package com.example.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

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
fun HomeScreen(navController: NavHostController) {
    val protein by remember { mutableStateOf("45/90 г") }
    val fat by remember { mutableStateOf("24/48 г") }
    val carbs by remember { mutableStateOf("62/125 г") }
    val calories by remember { mutableStateOf("1300 ккал") }
    val caloriesLeft by remember { mutableStateOf("800 ккал осталось") }
    val currentWeight by remember { mutableStateOf("61 кг") }
    val currentBMI by remember { mutableStateOf("19.8") }

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
                InfoCard("Текущий вес: $currentWeight", R.drawable.pencil)
                InfoCard("Текущий ИМТ: $currentBMI")
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