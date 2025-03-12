package com.example.myapplication.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen() {
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    val statistics = remember { mutableStateOf<Map<String, String>?>(null) }
    val currentMonth = remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CalendarView(
            selectedDate = selectedDate.value,
            currentMonth = currentMonth.value,
            onDateSelected = { date ->
                selectedDate.value = date
                statistics.value = getStatisticsForDate(date)
            },
            onMonthChange = { newMonth ->
                currentMonth.value = newMonth
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        selectedDate.value?.let { date ->
            statistics.value?.let { stats ->
                StatisticsView(date, stats)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    selectedDate: LocalDate?,
    currentMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val offset = firstDayOfMonth.dayOfWeek.value - 1

    Column(
        modifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onMonthChange(currentMonth.minusMonths(1))
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                onMonthChange(currentMonth.plusMonths(1))
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items((0 until 42).chunked(7)) { week ->
                Row {
                    week.forEach { dayIndex ->
                        val day = if (dayIndex >= offset && dayIndex < daysInMonth + offset) {
                            dayIndex - offset + 1
                        } else {
                            0
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .background(
                                    color = if (day == selectedDate?.dayOfMonth && currentMonth.month == selectedDate.month) Color.LightGray else Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    if (day > 0) {
                                        onDateSelected(currentMonth.atDay(day))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day > 0) {
                                Text(
                                    text = day.toString(),
                                    color = if (day == selectedDate?.dayOfMonth && currentMonth.month == selectedDate.month) Color.Black else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsView(date: LocalDate, statistics: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            statistics.forEach { (key, value) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = key,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007BFF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "График статистики",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "График будет здесь",
                color = Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getStatisticsForDate(date: LocalDate): Map<String, String> {
    return when (date.dayOfMonth) {
        16 -> mapOf(
            "Вес" to "56.5 кг",
            "Калории" to "1750 ккал",
            "Шаги" to "9570",
            "Вода" to "1950 мл",
            "Сон" to "7 ч 30 мин"
        )

        17 -> mapOf(
            "Вес" to "56.7 кг",
            "Калории" to "1800 ккал",
            "Шаги" to "9700",
            "Вода" to "2000 мл",
            "Сон" to "7 ч 45 мин"
        )

        18 -> mapOf(
            "Вес" to "56.6 кг",
            "Калории" to "1700 ккал",
            "Шаги" to "9400",
            "Вода" to "1900 мл",
            "Сон" to "7 ч 15 мин"
        )

        else -> mapOf(
            "Вес" to "Нет данных",
            "Калории" to "Нет данных",
            "Шаги" to "Нет данных",
            "Вода" to "Нет данных",
            "Сон" to "Нет данных"
        )
    }
}