package com.example.myapplication.vitamin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object VitaminColors {
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val ProgressBarColor = Color(0xFFBB86FC)
    val VitaminABackground = Color(0xFFFF6F61)
    val VitaminB1Background = Color(0xFF6B5B95)
    val VitaminB2Background = Color(0xFF88B04B)
    val VitaminB3Background = Color(0xFF705757)
}
object VitaminDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 4.dp
    val ProgressBarSize = 60.dp
    val VitaminBoxSize = 80.dp
    val SmileyTextSize = 60.sp
}

@Composable
fun VitaminScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(VitaminDimens.PaddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ВИТАМИНЫ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = VitaminColors.TextColor,
            modifier = Modifier.padding(bottom = VitaminDimens.PaddingMedium)
        )
        VitaminItem(vitamin = "A", progress = 0.8f, color = VitaminColors.VitaminABackground)
        VitaminItem(vitamin = "B1", progress = 0.5f, color = VitaminColors.VitaminB1Background)
        VitaminItem(vitamin = "B2", progress = 0.3f, color = VitaminColors.VitaminB2Background)
        VitaminItem(vitamin = "B3", progress = 0.9f, color = VitaminColors.VitaminB3Background)
    }
}
@Composable
fun VitaminItem(vitamin: String, progress: Float, color: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = VitaminDimens.PaddingSmall),
        shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
        color = VitaminColors.CardBackground,
        shadowElevation = VitaminDimens.CardElevation
    ) {
        Row(
            modifier = Modifier.padding(VitaminDimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(VitaminDimens.VitaminBoxSize)
                    .background(color, shape = RoundedCornerShape(VitaminDimens.CardCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vitamin,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitaminColors.TextColor
                )
            }
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .size(VitaminDimens.ProgressBarSize),
                color = VitaminColors.ProgressBarColor,
                strokeWidth = 6.dp,
            )
            Text(
                text = getSmiley(progress),
                fontSize = VitaminDimens.SmileyTextSize,
                modifier = Modifier.padding(start = VitaminDimens.PaddingMedium)
            )
        }
    }
}
fun getSmiley(progress: Float): String {
    return when {
        progress >= 0.8 -> "😊"
        progress >= 0.5 -> "😐"
        else -> "😞"
    }
}