package com.example.myapplication.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min
import kotlin.math.max

private val AccentColor = Color(0xFFFF6F42)
private val SecondaryColor = Color(0xFFBDB2D9)
private val ExcessColor = Color(0xFFE53935)

object SleepColors {
    val Orange = Color(0xFFFFA500)
    val LightGray = Color.LightGray
    val SemiTransparentWhite = Color(0x9AFFFFFF)
    val DarkGray = Color(0xFF3A3A3A)
    val ErrorRed = Color(0xFFE53935)
}

@Composable
fun SleepScreen() {
    var selectedRating by remember { mutableStateOf(0) }
    var startTimeHours by remember { mutableStateOf("") }
    var startTimeMinutes by remember { mutableStateOf("") }
    var endTimeHours by remember { mutableStateOf("") }
    var endTimeMinutes by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val startHoursError = showErrors && (startTimeHours.toIntOrNull() ?: 24) > 23
    val startMinutesError = showErrors && (startTimeMinutes.toIntOrNull() ?: 60) > 59
    val endHoursError = showErrors && (endTimeHours.toIntOrNull() ?: 24) > 23
    val endMinutesError = showErrors && (endTimeMinutes.toIntOrNull() ?: 60) > 59

    val (sleepDuration, isValid) = calculateSleepDuration(
        formatTimeForCalculation(startTimeHours, startTimeMinutes),
        formatTimeForCalculation(endTimeHours, endTimeMinutes)
    )

    val calculatedRating = calculateSleepQualityScore(
        duration = sleepDuration,
        userRating = selectedRating
    )

    val normalSleepDuration = 480
    val notNormalSleepDuration = 360
    val progress = min(sleepDuration.totalMinutes.toFloat() / normalSleepDuration, 1f)
    val excessProgress = if (sleepDuration.totalMinutes > normalSleepDuration) {
        (sleepDuration.totalMinutes - normalSleepDuration).toFloat() / normalSleepDuration
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Анализ сна",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = SleepColors.SemiTransparentWhite
        )

        Spacer(modifier = Modifier.height(24.dp))

        RatingStars(selectedRating) { selectedRating = it }

        Spacer(modifier = Modifier.height(24.dp))

        CircularSleepProgress(
            sleepHours = sleepDuration.format(),
            progress = if (showResult) progress else 0f,
            excessProgress = if (showResult) excessProgress else 0f
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SleepColors.DarkGray)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeInputRow(
                label = "Начало сна",
                hours = startTimeHours,
                minutes = startTimeMinutes,
                onHoursChange = {
                    if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 23) {
                        startTimeHours = it
                    }
                },
                onMinutesChange = {
                    if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 59) {
                        startTimeMinutes = it
                    }
                },
                hoursError = startHoursError,
                minutesError = startMinutesError
            )

            Spacer(modifier = Modifier.height(16.dp))

            TimeInputRow(
                label = "Конец сна",
                hours = endTimeHours,
                minutes = endTimeMinutes,
                onHoursChange = {
                    if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 23) {
                        endTimeHours = it
                    }
                },
                onMinutesChange = {
                    if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 59) {
                        endTimeMinutes = it
                    }
                },
                hoursError = endHoursError,
                minutesError = endMinutesError
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                showErrors = true
                if (selectedRating > 0 &&
                    startTimeHours.isNotEmpty() && startTimeMinutes.isNotEmpty() &&
                    endTimeHours.isNotEmpty() && endTimeMinutes.isNotEmpty() &&
                    !startHoursError && !startMinutesError &&
                    !endHoursError && !endMinutesError
                ) {
                    showResult = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SleepColors.Orange)
        ) {
            Text("Добавить", color = Color.White)
        }

        if (showResult && isValid) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Длительность сна: ${sleepDuration.format()}",
                color = SleepColors.LightGray,
                fontSize = 18.sp
            )
            Text(
                text = "Оценка сна: $calculatedRating/10",
                color = SleepColors.LightGray,
                fontSize = 18.sp
            )
            if (sleepDuration.totalMinutes > normalSleepDuration) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Вы слишком много спите!!!",
                    color = ExcessColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (sleepDuration.totalMinutes < notNormalSleepDuration) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Вы слишком мало спите!!!",
                    color = ExcessColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CircularSleepProgress(
    sleepHours: String,
    progress: Float,
    excessProgress: Float
) {
    Box(
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = SecondaryColor.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.linearGradient(colors = listOf(AccentColor, Color(0xFFFF9352))),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
            if (excessProgress > 0) {
                drawArc(
                    color = ExcessColor,
                    startAngle = -90f + progress * 360f,
                    sweepAngle = excessProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = sleepHours,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "часы сна", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun RatingStars(selectedRating: Int, onRatingChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..5).forEach { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index <= selectedRating) AccentColor else SecondaryColor,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRatingChange(index) }
            )
        }
    }
}

@Composable
fun TimeInputRow(
    label: String,
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    hoursError: Boolean = false,
    minutesError: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontWeight = FontWeight.Medium, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeTextField(
                value = hours,
                onValueChange = onHoursChange,
                placeholder = "чч",
                isError = hoursError
            )
            Text(" : ", fontSize = 20.sp, color = Color.White)
            TimeTextField(
                value = minutes,
                onValueChange = onMinutesChange,
                placeholder = "мм",
                isError = minutesError
            )
        }
        if (hoursError) {
            Text("Макс. 23", color = SleepColors.ErrorRed, fontSize = 12.sp)
        }
        if (minutesError) {
            Text("Макс. 59", color = SleepColors.ErrorRed, fontSize = 12.sp)
        }
    }
}

@Composable
fun TimeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false
) {
    var hasFocus by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(60.dp)
            .padding(4.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    if (newValue.length <= 2) {
                        onValueChange(newValue)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = if (isError) SleepColors.ErrorRed else Color.White
            ),
            modifier = Modifier
                .background(Color.Transparent)
                .onFocusChanged { hasFocus = it.isFocused },
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isError) SleepColors.ErrorRed.copy(alpha = 0.2f) else Color(
                                0xFF494358
                            )
                        )
                        .padding(vertical = 10.dp, horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    if (value.isEmpty() && !hasFocus) {
                        Text(placeholder, color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )
    }
}

fun formatTimeForCalculation(hours: String, minutes: String): Long {
    val hour = hours.toLongOrNull() ?: 0L
    val minute = minutes.toLongOrNull() ?: 0L
    return hour * 60 + minute
}

fun calculateSleepDuration(startTime: Long, endTime: Long): Pair<TimeDuration, Boolean> {
    val total = when {
        startTime == endTime -> 1440L
        endTime > startTime -> endTime - startTime
        else -> (1440 - startTime) + endTime
    }
    return TimeDuration(total).let { it to (total > 0) }
}

fun calculateSleepQualityScore(duration: TimeDuration, userRating: Int): Int {
    return if (duration.totalMinutes >= 420) userRating * 2 else max(userRating * 2 - 1, 1)
}

data class TimeDuration(val totalMinutes: Long) {
    fun format(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}ч ${"%02d".format(minutes)}м"
    }
}