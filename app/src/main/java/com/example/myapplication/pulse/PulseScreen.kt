package com.example.myapplication.pulse

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R

/**
 * Объект, содержащий цвета, используемые на экране пульса.
 */
object PulseColors {
    val CardBackground = Color(0xFF3A3347)
    val TextColor = Color.LightGray
    val White = Color.White
    val HeartColor = Color(0xFFFF5252)
    val ButtonBorder = Color(0xFFCE7B7B)
    val ButtonContent = Color(0xFFFF9999)
}

/**
 * Объект, содержащий размеры элементов интерфейса экрана пульса.
 */
object PulseDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp

    val CardCornerRadius = 16.dp
    val CardElevation = 8.dp
    val StatsCardHeight = 80.dp

    val IconSize = 24.dp
    val SmallIconSize = 20.dp
    val HeartSize = 240.dp
    val HeartContainerSize = 240.dp
    val ButtonHeight = 48.dp

    val TextSmall = 14.sp
    val TextMedium = 16.sp
    val TextLarge = 18.sp
    val TextTitle = 42.sp
}

/**
 * Экран отображения данных о пульсе.
 *
 * Показывает текущий пульс, максимальный и минимальный пульс за день, с возможностью перехода к данным о витаминах.
 *
 * @param navController Контроллер навигации.
 * @param viewModel ViewModel для управления данными о пульсе.
 */
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PulseDimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("pulse") { inclusive = true }
                    }
                },
                modifier = Modifier.size(PulseDimens.IconSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад на главный экран",
                    tint = PulseColors.TextColor
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PulseDimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ТЕКУЩИЙ ПУЛЬС",
                fontSize = PulseDimens.TextLarge,
                fontWeight = FontWeight.Bold,
                color = PulseColors.TextColor,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = PulseDimens.PaddingSmall)
            )

            HeartPulseBlock(currentPulse.value)

            Spacer(modifier = Modifier.height(PulseDimens.PaddingMedium))

            PulseStatsCard(
                title = "МАКСИМАЛЬНЫЙ",
                value = maxPulse.value,
                icon = Icons.Default.ArrowUpward,
                modifier = Modifier.padding(bottom = PulseDimens.PaddingSmall)
            )

            PulseStatsCard(
                title = "МИНИМАЛЬНЫЙ",
                value = minPulse.value,
                icon = Icons.Default.ArrowDownward,
                modifier = Modifier.padding(bottom = PulseDimens.PaddingLarge)
            )

            OutlinedButton(
                onClick = { navController.navigate("vitamin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PulseDimens.ButtonHeight)
                    .padding(horizontal = PulseDimens.PaddingLarge),
                shape = RoundedCornerShape(PulseDimens.CardCornerRadius),
                border = BorderStroke(1.dp, PulseColors.ButtonBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = PulseColors.ButtonContent
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = "Витамины",
                    modifier = Modifier.size(PulseDimens.SmallIconSize),
                    tint = PulseColors.ButtonContent
                )
                Spacer(modifier = Modifier.width(PulseDimens.PaddingSmall))
                Text(
                    text = "ДАННЫЕ О ВИТАМИНАХ",
                    fontSize = PulseDimens.TextMedium,
                    color = PulseColors.ButtonContent
                )
            }
        }
    }
}

/**
 * Компонент для отображения текущего пульса с анимацией сердца.
 *
 * @param currentPulse Текущий пульс.
 */
@Composable
fun HeartPulseBlock(currentPulse: Int) {
    val pulseDuration = if (currentPulse > 0) (60_000 / currentPulse) else 1000

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.size(PulseDimens.HeartContainerSize),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_heart),
            contentDescription = "Сердце",
            modifier = Modifier
                .size(PulseDimens.HeartSize)
                .scale(scale),
            colorFilter = ColorFilter.tint(PulseColors.HeartColor)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentPulse.toString(),
                fontSize = PulseDimens.TextTitle,
                fontWeight = FontWeight.Bold,
                color = PulseColors.TextColor
            )
            Text(
                text = "уд/мин",
                fontSize = PulseDimens.TextSmall,
                color = PulseColors.TextColor.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Карточка для отображения статистики пульса (максимального/минимального).
 *
 * @param title Заголовок карточки.
 * @param value Значение пульса.
 * @param icon Иконка для отображения.
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun PulseStatsCard(
    title: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(PulseDimens.StatsCardHeight),
        shape = RoundedCornerShape(PulseDimens.CardCornerRadius),
        color = PulseColors.CardBackground,
        border = BorderStroke(1.dp, PulseColors.TextColor.copy(alpha = 0.2f)),
        shadowElevation = PulseDimens.CardElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PulseDimens.PaddingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PulseColors.TextColor,
                    modifier = Modifier.size(PulseDimens.IconSize)
                )
                Spacer(modifier = Modifier.width(PulseDimens.PaddingMedium))
                Text(
                    text = title,
                    fontSize = PulseDimens.TextMedium,
                    fontWeight = FontWeight.Medium,
                    color = PulseColors.TextColor
                )
            }

            Text(
                text = "$value",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PulseColors.TextColor
            )
        }
    }
}