package com.example.myapplication.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen() {
    val currentDate = LocalDate.now()
    val selectedDate = remember { mutableStateOf(currentDate) }
    val statistics = remember { mutableStateOf(getStatisticsForDate(currentDate)) }
    val currentMonth = remember { mutableStateOf(YearMonth.from(currentDate)) }
    val showWeightChart = remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
        }

        item {
            StatisticsView(selectedDate.value, statistics.value)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { showWeightChart.value = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (showWeightChart.value) Color(0xFF1976D2) else Color.Gray
                    )
                ) {
                    Text("Вес")
                }
                TextButton(
                    onClick = { showWeightChart.value = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (!showWeightChart.value) Color(0xFF1976D2) else Color.Gray
                    )
                ) {
                    Text("Калории")
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showWeightChart.value) {
                    WeightChart(selectedDate.value)
                } else {
                    CaloriesChart(selectedDate.value)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val offset = firstDayOfMonth.dayOfWeek.value - 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Month")
                }

                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month")
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 400.dp)
            ) {
                LazyColumn {
                    items((0 until 42).chunked(7)) { week ->
                        Row {
                            week.forEach { dayIndex ->
                                val day = if (dayIndex >= offset && dayIndex < daysInMonth + offset) {
                                    dayIndex - offset + 1
                                } else {
                                    0
                                }
                                val date = if (day > 0) currentMonth.atDay(day) else null
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .background(
                                            color = when {
                                                isSelected -> Color.LightGray
                                                isToday -> Color(0xFFE3F2FD)
                                                else -> Color.Transparent
                                            },
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clickable {
                                            if (day > 0) onDateSelected(currentMonth.atDay(day))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day > 0) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = day.toString(),
                                                color = when {
                                                    isSelected -> Color.Black
                                                    isToday -> Color(0xFF1976D2)
                                                    else -> Color.Gray
                                                },
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isToday) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(
                                                            color = Color(0xFF1976D2),
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
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
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))),
            fontSize = 16.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (statistics.isEmpty() || statistics.values.all { it == "Нет данных" }) {
            Text(
                text = "Нет данных за выбранный день",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(title = "Вес", value = statistics["Вес"] ?: "")
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                StatItem(title = "Калории", value = statistics["Калории"] ?: "")
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                StatItem(title = "Шаги", value = statistics["Шаги"] ?: "")
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                StatItem(title = "Вода", value = statistics["Вода"] ?: "")
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                StatItem(title = "Сон", value = statistics["Сон"] ?: "")
            }
        }
    }
}
@Composable
fun StatItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF555555)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}
@Composable
fun LineChart(
    dataPoints: List<Float>,
    yAxisLabelFormatter: (Float) -> String,
    xAxisLabels: List<String>,
    yAxisSteps: Int = 4,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        val leftOffset = 70f
        val bottomOffset = 60f
        val topOffset = 20f

        val canvasWidth = size.width - leftOffset
        val canvasHeight = size.height - bottomOffset - topOffset

        val maxY = dataPoints.maxOrNull() ?: 1f
        val minY = dataPoints.minOrNull() ?: 0f
        val yRange = maxY - minY

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        val axisTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        for (i in 0..yAxisSteps) {
            val yValue = maxY - (yRange * i / yAxisSteps)
            val yPos = topOffset + (canvasHeight * i / yAxisSteps)

            drawContext.canvas.nativeCanvas.drawText(
                yAxisLabelFormatter(yValue),
                leftOffset - 10f,
                yPos + 10f,
                textPaint
            )

            drawLine(
                start = Offset(leftOffset, yPos),
                end = Offset(size.width, yPos),
                color = Color.LightGray,
                strokeWidth = 1f
            )
        }
        val xStep = canvasWidth / (xAxisLabels.size - 1)
        xAxisLabels.forEachIndexed { index, label ->
            drawContext.canvas.nativeCanvas.drawText(
                label,
                leftOffset + xStep * index,
                topOffset + canvasHeight + 30f,
                axisTextPaint
            )
        }
        val points = dataPoints.mapIndexed { index, value ->
            val x = leftOffset + (canvasWidth / (dataPoints.size - 1)) * index
            val y = topOffset + (canvasHeight * (1 - (value - minY) / (yRange.takeIf { it > 0f } ?: 1f)))
            Offset(x, y)
        }
        for (i in 1 until points.size) {
            drawLine(
                start = points[i - 1],
                end = points[i],
                color = Color(0xFF1976D2),
                strokeWidth = 3f
            )
        }
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF1976D2),
                radius = 6f,
                center = point
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightChart(date: LocalDate) {
    val weights = List(7) { 60f + Random.nextFloat() * 5f }
    val days = List(7) { index -> date.minusDays((6 - index).toLong()).dayOfMonth.toString() }

    LineChart(
        dataPoints = weights,
        xAxisLabels = days,
        yAxisLabelFormatter = { "%.1f кг".format(it) }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CaloriesChart(date: LocalDate) {
    val calories = List(7) { 1800f + Random.nextFloat() * 400f }
    val days = List(7) { index -> date.minusDays((6 - index).toLong()).dayOfMonth.toString() }

    LineChart(
        dataPoints = calories,
        xAxisLabels = days,
        yAxisLabelFormatter = { "%.0f ккал".format(it) }
    )
}



@RequiresApi(Build.VERSION_CODES.O)
fun getStatisticsForDate(date: LocalDate): Map<String, String>  {
    val random = Random(date.toEpochDay().toInt())
    return mapOf(
        "Вес" to "%.1f кг".format(55.0 + random.nextDouble() * 3),
        "Калории" to "%,d ккал".format(1500 + random.nextInt(1000)),
        "Шаги" to "%,d".format(5000 + random.nextInt(8000)),
        "Вода" to "%,d мл".format(1500 + random.nextInt(1000)),
        "Сон" to "%d ч %02d мин".format(6 + random.nextInt(3), random.nextInt(60))
    )
}