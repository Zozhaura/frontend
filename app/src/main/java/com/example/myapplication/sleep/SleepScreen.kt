@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SleepColors {
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val SelectedColor = Color(0xFFBB86FC)
    val UnselectedColor = Color(0xFF786F93)
    val NumberBlockColor = Color(0xFF3A3A3A)
}

object SleepDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 4.dp
    val NumberSize = 40.dp
    val TextFieldWidth = 120.dp
}

@Composable
fun SleepScreen() {
    var selectedRating by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    val sleepDuration = calculateSleepDuration(startTime, endTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SleepDimens.PaddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SleepDimens.PaddingMedium),
            shape = RoundedCornerShape(SleepDimens.CardCornerRadius),
            color = SleepColors.NumberBlockColor,
            shadowElevation = SleepDimens.CardElevation
        ) {
            Column(
                modifier = Modifier.padding(SleepDimens.PaddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Как вам сегодня спалось?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleepColors.TextColor,
                    modifier = Modifier.padding(bottom = SleepDimens.PaddingMedium)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = SleepDimens.PaddingSmall),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..5) {
                        NumberBox(
                            number = i,
                            isSelected = i == selectedRating,
                            onClick = { selectedRating = i }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = SleepDimens.PaddingMedium),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 6..10) {
                        NumberBox(
                            number = i,
                            isSelected = i == selectedRating,
                            onClick = { selectedRating = i }
                        )
                    }
                }

                Text(
                    text = when (selectedRating) {
                        in 8..10 -> "Отлично!"
                        in 5..7 -> "Неплохо!"
                        in 1..4 -> "Плохо!"
                        else -> "Оцените ваш сон"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleepColors.TextColor,
                    modifier = Modifier.padding(bottom = SleepDimens.PaddingMedium)
                )
            }
        }

        SleepTimeInput(
            startTime = startTime,
            endTime = endTime,
            onStartTimeChange = { startTime = it },
            onEndTimeChange = { endTime = it }
        )

        if (selectedRating > 0 && startTime.isNotEmpty() && endTime.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SleepDimens.PaddingMedium),
                shape = RoundedCornerShape(SleepDimens.CardCornerRadius),
                color = SleepColors.CardBackground,
                shadowElevation = SleepDimens.CardElevation
            ) {
                Column(
                    modifier = Modifier.padding(SleepDimens.PaddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Оценим ваше качество сна:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleepColors.TextColor,
                        modifier = Modifier.padding(bottom = SleepDimens.PaddingSmall)
                    )

                    Text(
                        text = when (selectedRating) {
                            in 8..10 -> "😊"
                            in 5..7 -> "😐"
                            else -> "😞"
                        },
                        fontSize = 40.sp,
                        modifier = Modifier.padding(bottom = SleepDimens.PaddingSmall)
                    )

                    Text(
                        text = "Продолжительность сна: ${sleepDuration.hours} часов ${sleepDuration.minutes} минут",
                        fontSize = 18.sp,
                        color = SleepColors.TextColor
                    )
                }
            }
        }
    }
}

fun calculateSleepDuration(startTime: String, endTime: String): SleepDuration {
    if (startTime.isEmpty() || endTime.isEmpty()) return SleepDuration(0, 0)

    val startParts = startTime.split(":")
    val endParts = endTime.split(":")

    if (startParts.size != 2 || endParts.size != 2) return SleepDuration(0, 0)

    val startHours = startParts[0].toIntOrNull() ?: 0
    val startMinutes = startParts[1].toIntOrNull() ?: 0
    val endHours = endParts[0].toIntOrNull() ?: 0
    val endMinutes = endParts[1].toIntOrNull() ?: 0

    val totalStartMinutes = startHours * 60 + startMinutes
    val totalEndMinutes = endHours * 60 + endMinutes

    var durationMinutes = totalEndMinutes - totalStartMinutes
    if (durationMinutes < 0) {
        durationMinutes += 24 * 60
    }

    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60

    return SleepDuration(hours, minutes)
}
data class SleepDuration(val hours: Int, val minutes: Int)

@Composable
fun NumberBox(number: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(SleepDimens.NumberSize)
            .background(
                color = if (isSelected) SleepColors.SelectedColor else SleepColors.UnselectedColor,
                shape = RoundedCornerShape(SleepDimens.CardCornerRadius)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SleepColors.TextColor
        )
    }
}

@Composable
fun SleepTimeInput(
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = SleepDimens.PaddingMedium),
        shape = RoundedCornerShape(SleepDimens.CardCornerRadius),
        color = SleepColors.CardBackground,
        shadowElevation = SleepDimens.CardElevation
    ) {
        Column(
            modifier = Modifier.padding(SleepDimens.PaddingMedium)
        ) {
            Text(
                text = "Начало сна:",
                fontSize = 18.sp,
                color = SleepColors.TextColor,
                modifier = Modifier.padding(bottom = SleepDimens.PaddingSmall)
            )
            TimeTextField(
                time = startTime,
                onTimeChange = onStartTimeChange,
                modifier = Modifier
                    .width(SleepDimens.TextFieldWidth)
                    .padding(bottom = SleepDimens.PaddingMedium)
            )

            Text(
                text = "Конец сна:",
                fontSize = 18.sp,
                color = SleepColors.TextColor,
                modifier = Modifier.padding(bottom = SleepDimens.PaddingSmall)
            )
            TimeTextField(
                time = endTime,
                onTimeChange = onEndTimeChange,
                modifier = Modifier.width(SleepDimens.TextFieldWidth)
            )
        }
    }
}

@Composable
fun TimeTextField(
    time: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var localTime by remember { mutableStateOf(time) }

    LaunchedEffect(time) {
        localTime = time
    }

    OutlinedTextField(
        value = localTime,
        onValueChange = { newValue ->
            val formattedValue = formatTimeInput(newValue)
            if (isValidTime(formattedValue)) {
                localTime = formattedValue
                onTimeChange(formattedValue)
            }
        },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        placeholder = { Text("HH:MM", color = SleepColors.TextColor) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = SleepColors.SelectedColor,
            unfocusedBorderColor = SleepColors.UnselectedColor
        ),
        visualTransformation = TimeVisualTransformation()
    )
}

fun formatTimeInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.isEmpty() -> ""
        digitsOnly.length <= 2 -> digitsOnly
        else -> "${digitsOnly.take(2)}:${digitsOnly.drop(2).take(2)}"
    }
}

fun isValidTime(time: String): Boolean {
    if (time.isEmpty()) return true

    val parts = time.split(":")
    if (parts.size > 2) return false

    val hours = parts[0].toIntOrNull() ?: return false
    if (hours !in 0..23) return false

    if (parts.size == 2) {
        val minutes = parts[1].toIntOrNull() ?: return false
        if (minutes !in 0..59) return false
    }

    return true
}

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formattedText = formatTimeInput(text.text)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> 5
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> 4
                }
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}