package com.example.myapplication.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.diary.DiaryStorage
import com.example.myapplication.utils.TokenManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Файл, содержащий экран календаря и графики статистики для приложения.
 *
 * Этот файл предоставляет UI для отображения календаря, статистики питания, сна и веса за выбранный период.
 * Поддерживает недельный и месячный вид графиков. Требуется API 26 (Android 8.0) и выше из-за использования `java.time`.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavController
) {
    val currentDate = LocalDate.now()
    val selectedDate = remember { mutableStateOf(currentDate) }
    val currentMonth = remember { mutableStateOf(YearMonth.from(currentDate)) }
    val chartDisplayType = remember { mutableStateOf("Weekly") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("calendar") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to home screen",
                    tint = Color.LightGray
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CalendarView(
                    selectedDate = selectedDate.value,
                    currentMonth = currentMonth.value,
                    onDateSelected = { date ->
                        selectedDate.value = date
                    },
                    onMonthChange = { newMonth ->
                        currentMonth.value = newMonth
                    }
                )
            }

            item {
                StatisticsView(selectedDate.value)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { chartDisplayType.value = "Weekly" },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (chartDisplayType.value == "Weekly") Color(0xFF1976D2) else Color.Gray
                        )
                    ) {
                        Text("За неделю", fontSize = 16.sp)
                    }
                    TextButton(
                        onClick = { chartDisplayType.value = "Monthly" },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (chartDisplayType.value == "Monthly") Color(0xFF1976D2) else Color.Gray
                        )
                    ) {
                        Text("За месяц", fontSize = 16.sp)
                    }
                }
            }

            when (chartDisplayType.value) {
                "Weekly" -> {
                    item {
                        NutritionWeeklyChart("Калории", Color(0xFF1976D2))
                    }
                    item {
                        NutritionWeeklyChart("Белки", Color(0xFF6B5B95))
                    }
                    item {
                        NutritionWeeklyChart("Жиры", Color(0xFFFF6F61))
                    }
                    item {
                        NutritionWeeklyChart("Углеводы", Color(0xFF4CAF50))
                    }
                    item {
                        SleepWeeklyChart(Color(0xFFFFA500))
                    }
                    item {
                        WeightWeeklyChart(Color(0xFF9575CD))
                    }
                }
                "Monthly" -> {
                    item {
                        NutritionMonthlyChart("Калории", Color(0xFF1976D2))
                    }
                    item {
                        NutritionMonthlyChart("Белки", Color(0xFF6B5B95))
                    }
                    item {
                        NutritionMonthlyChart("Жиры", Color(0xFFFF6F61))
                    }
                    item {
                        NutritionMonthlyChart("Углеводы", Color(0xFF4CAF50))
                    }
                    item {
                        SleepMonthlyChart(Color(0xFFFFA500))
                    }
                    item {
                        WeightMonthlyChart(Color(0xFF9575CD))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Отображает календарь для выбора даты.
 *
 * @param selectedDate Текущая выбранная дата.
 * @param currentMonth Текущий месяц для отображения.
 * @param onDateSelected Callback, вызываемый при выборе даты.
 * @param onMonthChange Callback, вызываемый при изменении месяца.
 */
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
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 360.dp)
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
                                        .padding(3.dp)
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
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                            if (isToday) {
                                                Spacer(modifier = Modifier.height(1.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
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

/**
 * Отображает статистику питания за выбранную дату.
 *
 * @param date Дата, для которой отображается статистика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsView(date: LocalDate) {
    val context = LocalContext.current
    val nutritionStats = DiaryStorage.getStatsForDate(context, date.toString())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))),
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (nutritionStats == null) {
            Text(
                text = "Нет данных за выбранный день",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (nutritionStats != null) {
                    StatItem(title = "Калории", value = "${nutritionStats.totalCalories.toInt()} ккал")
                    StatItem(title = "Белки", value = "${nutritionStats.totalProteins.toInt()} г")
                    StatItem(title = "Жиры", value = "${nutritionStats.totalFats.toInt()} г")
                    StatItem(title = "Углеводы", value = "${nutritionStats.totalCarbs.toInt()} г")
                }
            }
        }
    }
}

/**
 * Отображает одну строку статистики с названием и значением.
 *
 * @param title Название статистики (например, "Калории").
 * @param value Значение статистики (например, "500 ккал").
 */
@Composable
fun StatItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF555555)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
    }
}

/**
 * Отображает элемент легенды для графика.
 *
 * @param color Цвет линии или маркера.
 * @param label Название элемента (например, "Калории").
 */
@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.padding(2.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

/**
 * Отображает линейный график для визуализации данных.
 *
 * @param dataPoints Список значений для построения графика.
 * @param yAxisLabelFormatter Форматтер для меток оси Y.
 * @param xAxisLabels Метки для оси X.
 * @param xAxisLabelStep Шаг для отображения меток оси X.
 * @param lineColor Цвет линии графика.
 * @param yAxisSteps Количество шагов на оси Y.
 * @param modifier Модификатор для настройки внешнего вида.
 */
@Composable
fun SingleLineChart(
    dataPoints: List<Float>,
    yAxisLabelFormatter: (Float) -> String,
    xAxisLabels: List<String>,
    xAxisLabelStep: Int,
    lineColor: Color,
    yAxisSteps: Int = 5,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .shadow(4.dp, RoundedCornerShape(6.dp))
    ) {
        val leftOffset = 50f
        val rightOffset = 10f
        val bottomOffset = 40f
        val topOffset = 20f

        val canvasWidth = size.width - leftOffset - rightOffset
        val canvasHeight = size.height - bottomOffset - topOffset

        val maxY = dataPoints.maxOrNull() ?: 1f
        val minY = 0f
        val yRange = maxY - minY
        val yStep = if (yRange > 0) canvasHeight / yRange else 1f

        for (i in 0..yAxisSteps) {
            val yValue = maxY - (yRange * i / yAxisSteps)
            val yPos = topOffset + (canvasHeight * i / yAxisSteps)

            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(leftOffset, yPos),
                end = Offset(size.width - rightOffset, yPos),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                yAxisLabelFormatter(yValue),
                leftOffset - 8f,
                yPos + 6f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        val xStep = canvasWidth / (xAxisLabels.size - 1)
        xAxisLabels.forEachIndexed { index, label ->
            if (index % xAxisLabelStep == 0) {
                val xPos = leftOffset + xStep * index
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(xPos, topOffset),
                    end = Offset(xPos, size.height - bottomOffset),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    xPos,
                    size.height - 8f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        val shadowPath = Path().apply {
            dataPoints.mapIndexed { index, value ->
                val x = leftOffset + (canvasWidth / (dataPoints.size - 1)) * index
                val y = topOffset + (canvasHeight - (value - minY) * yStep)
                if (index == 0) moveTo(x, y)
                else {
                    val prevValue = dataPoints[index - 1]
                    val prevX = leftOffset + (canvasWidth / (dataPoints.size - 1)) * (index - 1)
                    val prevY = topOffset + (canvasHeight - (prevValue - minY) * yStep)
                    val midX = (prevX + x) / 2
                    cubicTo(midX, prevY, midX, y, x, y)
                }
            }
        }
        drawPath(
            path = shadowPath,
            color = Color.Black.copy(alpha = 0.2f),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        val points = dataPoints.mapIndexed { index, value ->
            val x = leftOffset + (canvasWidth / (dataPoints.size - 1)) * index
            val y = topOffset + (canvasHeight - (value - minY) * yStep)
            Offset(x, y)
        }
        val path = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y)
                else {
                    val prev = points[index - 1]
                    val midX = (prev.x + point.x) / 2
                    cubicTo(midX, prev.y, midX, point.y, point.x, point.y)
                }
            }
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(lineColor.copy(alpha = 0.9f), lineColor.copy(alpha = 0.6f)),
                start = Offset(leftOffset, topOffset),
                end = Offset(size.width - rightOffset, topOffset + canvasHeight)
            ),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
        points.forEach { point ->
            drawCircle(color = lineColor, radius = 8f, center = point, style = Stroke(width = 3f))
            drawCircle(color = Color.White, radius = 4f, center = point)
        }
    }
}

/**
 * Отображает недельный график питания (калории, белки, жиры, углеводы).
 *
 * @param label Название статистики (например, "Калории").
 * @param lineColor Цвет линии графика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NutritionWeeklyChart(label: String, lineColor: Color) {
    val context = LocalContext.current
    val allStats = DiaryStorage.getAllStats(context)
    val today = LocalDate.now()
    val startDate = today.minusDays(6)

    val filteredStats = allStats.filter {
        val date = LocalDate.parse(it.date)
        !date.isBefore(startDate) && !date.isAfter(today)
    }

    val days = mutableListOf<String>()
    val dataPoints = mutableListOf<Float>()
    for (i in 0..6) {
        val date = today.minusDays(i.toLong())
        val dateStr = date.toString()
        days.add(date.dayOfMonth.toString())
        val stat = filteredStats.find { it.date == dateStr }
        val value = when (label) {
            "Калории" -> stat?.totalCalories?.toFloat() ?: 0f
            "Белки" -> stat?.totalProteins?.toFloat() ?: 0f
            "Жиры" -> stat?.totalFats?.toFloat() ?: 0f
            "Углеводы" -> stat?.totalCarbs?.toFloat() ?: 0f
            else -> 0f
        }
        dataPoints.add(value)
    }

    days.reverse()
    dataPoints.reverse()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "$label за неделю",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            LegendItem(color = lineColor, label = label)
        }
        SingleLineChart(
            dataPoints = dataPoints,
            yAxisLabelFormatter = { "${it.roundToInt()}" },
            xAxisLabels = days,
            xAxisLabelStep = 1,
            lineColor = lineColor
        )
    }
}

/**
 * Отображает месячный график питания (калории, белки, жиры, углеводы).
 *
 * @param label Название статистики (например, "Калории").
 * @param lineColor Цвет линии графика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NutritionMonthlyChart(label: String, lineColor: Color) {
    val context = LocalContext.current
    val allStats = DiaryStorage.getAllStats(context)
    val today = LocalDate.now()
    val startDate = today.minusDays(29)

    val filteredStats = allStats.filter {
        val date = LocalDate.parse(it.date)
        !date.isBefore(startDate) && !date.isAfter(today)
    }

    val days = mutableListOf<String>()
    val dataPoints = mutableListOf<Float>()
    for (i in 0..29) {
        val date = today.minusDays(i.toLong())
        val dateStr = date.toString()
        if (i % 3 == 0) {
            days.add(date.dayOfMonth.toString())
        }
        val stat = filteredStats.find { it.date == dateStr }
        val value = when (label) {
            "Калории" -> stat?.totalCalories?.toFloat() ?: 0f
            "Белки" -> stat?.totalProteins?.toFloat() ?: 0f
            "Жиры" -> stat?.totalFats?.toFloat() ?: 0f
            "Углеводы" -> stat?.totalCarbs?.toFloat() ?: 0f
            else -> 0f
        }
        dataPoints.add(value)
    }

    days.reverse()
    dataPoints.reverse()

    val xAxisLabels = (0..29 step 3).map { days.getOrNull(it / 3) ?: "" }.toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "$label за месяц",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            LegendItem(color = lineColor, label = label)
        }
        SingleLineChart(
            dataPoints = dataPoints,
            yAxisLabelFormatter = { "${it.roundToInt()}" },
            xAxisLabels = xAxisLabels,
            xAxisLabelStep = 1,
            lineColor = lineColor
        )
    }
}

/**
 * Генерирует имя файла на основе токена пользователя.
 *
 * @param baseName Базовое имя файла.
 * @param context Контекст приложения.
 * @return Имя файла с добавленным токеном.
 */
private fun getUserFileName(baseName: String, context: android.content.Context): String {
    val token = TokenManager.getToken(context) ?: "default_user"
    val sanitizedToken = token.replace("[^a-zA-Z0-9]".toRegex(), "_")
    return "${baseName}_${sanitizedToken}.json"
}

/**
 * Загружает статистику сна из локального файла.
 *
 * @param context Контекст приложения.
 * @param gson Экземпляр Gson для десериализации.
 * @return Список записей статистики сна.
 */
private fun loadSleepStats(context: android.content.Context, gson: Gson): List<Map<String, Any>> {
    val fileName = getUserFileName("sleep_stats", context)
    val file = File(context.filesDir, fileName)
    if (!file.exists()) return emptyList()
    val json = file.readText()
    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
    return gson.fromJson(json, type) ?: emptyList()
}

/**
 * Загружает статистику веса из локального файла.
 *
 * @param context Контекст приложения.
 * @param gson Экземпляр Gson для десериализации.
 * @return Список записей статистики веса.
 */
private fun loadWeightStats(context: android.content.Context, gson: Gson): List<Map<String, Any>> {
    val fileName = getUserFileName("weight_stats", context)
    val file = File(context.filesDir, fileName)
    if (!file.exists()) return emptyList()
    val json = file.readText()
    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
    return gson.fromJson(json, type) ?: emptyList()
}

/**
 * Отображает недельный график продолжительности сна.
 *
 * @param lineColor Цвет линии графика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SleepWeeklyChart(lineColor: Color) {
    val context = LocalContext.current
    val gson = Gson()
    val allStats = loadSleepStats(context, gson)
    val today = LocalDate.now()
    val startDate = today.minusDays(6)

    val filteredStats = allStats.filter {
        val date = LocalDate.parse(it["date"] as String)
        !date.isBefore(startDate) && !date.isAfter(today)
    }

    val days = mutableListOf<String>()
    val dataPoints = mutableListOf<Float>()
    for (i in 0..6) {
        val date = today.minusDays(i.toLong())
        val dateStr = date.toString()
        days.add(date.dayOfMonth.toString())
        val stat = filteredStats.find { it["date"] == dateStr }
        val durationInMinutes = (stat?.get("duration_minutes") as? Double)?.toFloat() ?: 0f
        val durationInHours = durationInMinutes / 60f
        dataPoints.add(durationInHours)
    }

    days.reverse()
    dataPoints.reverse()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Сон за неделю (часы)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            LegendItem(color = lineColor, label = "Сон")
        }
        SingleLineChart(
            dataPoints = dataPoints,
            yAxisLabelFormatter = { String.format("%.1f ч", it) },
            xAxisLabels = days,
            xAxisLabelStep = 1,
            lineColor = lineColor
        )
    }
}

/**
 * Отображает месячный график продолжительности сна.
 *
 * @param lineColor Цвет линии графика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SleepMonthlyChart(lineColor: Color) {
    val context = LocalContext.current
    val gson = Gson()
    val allStats = loadSleepStats(context, gson)
    val today = LocalDate.now()
    val startDate = today.minusDays(29)

    val filteredStats = allStats.filter {
        val date = LocalDate.parse(it["date"] as String)
        !date.isBefore(startDate) && !date.isAfter(today)
    }

    val days = mutableListOf<String>()
    val dataPoints = mutableListOf<Float>()
    for (i in 0..29) {
        val date = today.minusDays(i.toLong())
        val dateStr = date.toString()
        if (i % 3 == 0) {
            days.add(date.dayOfMonth.toString())
        }
        val stat = filteredStats.find { it["date"] == dateStr }
        val durationInMinutes = (stat?.get("duration_minutes") as? Double)?.toFloat() ?: 0f
        val durationInHours = durationInMinutes / 60f
        dataPoints.add(durationInHours)
    }

    days.reverse()
    dataPoints.reverse()

    val xAxisLabels = (0..29 step 3).map { days.getOrNull(it / 3) ?: "" }.toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Сон за месяц (часы)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            LegendItem(color = lineColor, label = "Сон")
        }
        SingleLineChart(
            dataPoints = dataPoints,
            yAxisLabelFormatter = { String.format("%.1f ч", it) },
            xAxisLabels = xAxisLabels,
            xAxisLabelStep = 1,
            lineColor = lineColor
        )
    }
}

/**
 * Отображает недельный график веса.
 *
 * @param lineColor Цвет линии графика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightWeeklyChart(lineColor: Color) {
    val context = LocalContext.current
    val gson = Gson()
    val allStats = loadWeightStats(context, gson)
    val today = LocalDate.now()
    val startDate = today.minusDays(6)

    val filteredStats = allStats.filter {
        val date = LocalDate.parse(it["date"] as String)
        !date.isBefore(startDate) && !date.isAfter(today)
    }

    val days = mutableListOf<String>()
    val dataPoints = mutableListOf<Float>()
    for (i in 0..6) {
        val date = today.minusDays(i.toLong())
        val dateStr = date.toString()
        days.add(date.dayOfMonth.toString())
        val stat = filteredStats.find { it["date"] == dateStr }
        val weight = (stat?.get("weight") as? Double)?.toFloat() ?: 0f
        dataPoints.add(weight)
    }

    days.reverse()
    dataPoints.reverse()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Вес за неделю (кг)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            LegendItem(color = lineColor, label = "Вес")
        }
        SingleLineChart(
            dataPoints = dataPoints,
            yAxisLabelFormatter = { "${it.roundToInt()} кг" },
            xAxisLabels = days,
            xAxisLabelStep = 1,
            lineColor = lineColor
        )
    }
}

/**
 * Отображает месячный график веса.
 *
 * @param lineColor Цвет линии графика.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightMonthlyChart(lineColor: Color) {
    val context = LocalContext.current
    val gson = Gson()
    val allStats = loadWeightStats(context, gson)
    val today = LocalDate.now()
    val startDate = today.minusDays(29)

    val filteredStats = allStats.filter {
        val date = LocalDate.parse(it["date"] as String)
        !date.isBefore(startDate) && !date.isAfter(today)
    }

    val days = mutableListOf<String>()
    val dataPoints = mutableListOf<Float>()
    for (i in 0..29) {
        val date = today.minusDays(i.toLong())
        val dateStr = date.toString()
        if (i % 3 == 0) {
            days.add(date.dayOfMonth.toString())
        }
        val stat = filteredStats.find { it["date"] == dateStr }
        val weight = (stat?.get("weight") as? Double)?.toFloat() ?: 0f
        dataPoints.add(weight)
    }

    days.reverse()
    dataPoints.reverse()

    val xAxisLabels = (0..29 step 3).map { days.getOrNull(it / 3) ?: "" }.toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "Вес за месяц (кг)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            LegendItem(color = lineColor, label = "Вес")
        }
        SingleLineChart(
            dataPoints = dataPoints,
            yAxisLabelFormatter = { "${it.roundToInt()} кг" },
            xAxisLabels = xAxisLabels,
            xAxisLabelStep = 1,
            lineColor = lineColor
        )
    }
}