 package com.example.myapplication.pulse

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R

object AppColors {
    val CardBackground = Color(0xFF494358)
    val HeartBackground = Color(0xB4615977)
    val TextColor = Color.LightGray
    val ButtonColor = Color(0xFF494358)
}

object AppDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val HeartSize = 375.dp
    val ButtonHeight = 56.dp
    val ButtonPadding = 32.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 4.dp
}

 @Composable
 fun PulseScreen(
     navController: NavController,
     viewModel: PulseViewModel = viewModel()
 ) {
     val currentPulse = viewModel.pulseState.collectAsState()
     val maxPulse = viewModel.maxPulse.collectAsState()
     val minPulse = viewModel.minPulse.collectAsState()

     Column(
         modifier = Modifier
             .fillMaxSize()
             .padding(AppDimens.PaddingMedium),
         horizontalAlignment = Alignment.CenterHorizontally
     ) {
         Text(
             text = "ВАШ ТЕКУЩИЙ ПУЛЬС",
             fontSize = 24.sp,
             fontWeight = FontWeight.Bold,
             color = AppColors.TextColor,
             modifier = Modifier.padding(bottom = AppDimens.PaddingMedium)
         )
         HeartPulseBlock(currentPulse.value)
         PulseInfoBlock("Максимальный пульс за день: ${maxPulse.value}")
         PulseInfoBlock("Минимальный пульс за день: ${minPulse.value}")
         Button(
             onClick = { navController.navigate("vitamin") },
             modifier = Modifier
                 .fillMaxWidth()
                 .height(AppDimens.ButtonHeight)
                 .padding(horizontal = AppDimens.ButtonPadding),
             colors = ButtonDefaults.buttonColors(
                 containerColor = AppColors.ButtonColor
             )
         ) {
             Text(
                 text = "ДАННЫЕ О ВИТАМИНАХ",
                 fontSize = 18.sp,
                 fontWeight = FontWeight.Bold,
                 color = Color.White
             )
         }
     }
 }
@Composable
fun HeartPulseBlock(currentPulse: Int) {
    val pulseDuration = if (currentPulse > 0) (60_000 / currentPulse) else 1000

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier
            .size(AppDimens.HeartSize)
            .padding(bottom = AppDimens.PaddingMedium),
        shape = RoundedCornerShape(AppDimens.CardCornerRadius),
        color = AppColors.HeartBackground,
        shadowElevation = AppDimens.CardElevation
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "Heart Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
            )
            Text(
                text = currentPulse.toString(),
                fontSize = 55.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun PulseInfoBlock(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDimens.PaddingSmall),
        shape = RoundedCornerShape(AppDimens.CardCornerRadius),
        color = AppColors.CardBackground,
        shadowElevation = AppDimens.CardElevation
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = AppColors.TextColor,
            modifier = Modifier.padding(AppDimens.PaddingMedium)
        )
    }
}