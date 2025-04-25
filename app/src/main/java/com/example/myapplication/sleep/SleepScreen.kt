package com.example.myapplication.sleep

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.utils.TokenManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private val AccentColor = Color(0xFFFF6F42)
private val SecondaryColor = Color(0xFFBDB2D9)
private val ExcessColor = Color(0xFFE53935)

/**
 * Объект, содержащий цвета, используемые на экране сна.
 */
object SleepColors {
    val Orange = Color(0xFFFFA500)
    val LightGray = Color.LightGray
    val SemiTransparentWhite = Color(0x9AFFFFFF)
    val DarkGray = Color(0xFF3A3A3A)
    val ErrorRed = Color(0xFFE53935)
}

/**
 * Класс данных для хранения записи о сне.
 *
 * @param startTime Время начала сна в минутах с начала суток.
 * @param endTime Время окончания сна в минутах с начала суток.
 * @param rating Оценка сна (от 1 до 5).
 * @param timestamp Время создания записи (по умолчанию текущее время).
 */
data class SleepRecord(
    val startTime: Long,
    val endTime: Long,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Экран для анализа и записи данных о сне.
 *
 * Позволяет пользователю вводить время сна, оценивать его качество и просматривать историю.
 *
 * @param navController Контроллер навигации для перехода между экранами.
 */
@Composable
fun SleepScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val gson = Gson()
    val prefs = context.getSharedPreferences("SleepPrefs", Context.MODE_PRIVATE)
    var sleepRecords by remember {
        mutableStateOf(loadSleepRecords(prefs, gson, context))
    }

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
    } else 0f

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("sleep") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад на главный экран",
                    tint = SleepColors.SemiTransparentWhite
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
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
                    excessProgress = if (showResult) excessProgress else 0f,
                    qualityScore = if (showResult) calculatedRating else 0
                )

                Spacer(modifier = Modifier.height(24.dp))

                TimeInputSection(
                    startTimeHours, startTimeMinutes, endTimeHours, endTimeMinutes,
                    onStartHoursChange = { if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 23) startTimeHours = it },
                    onStartMinutesChange = { if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 59) startTimeMinutes = it },
                    onEndHoursChange = { if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 23) endTimeHours = it },
                    onEndMinutesChange = { if (it.isEmpty() || (it.toIntOrNull() ?: 0) <= 59) endTimeMinutes = it },
                    startHoursError, startMinutesError, endHoursError, endMinutesError
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showErrors = true
                        if (isInputValid(selectedRating, startTimeHours, startTimeMinutes,
                                endTimeHours, endTimeMinutes, startHoursError, startMinutesError,
                                endHoursError, endMinutesError)) {
                            showResult = true
                            val newRecord = SleepRecord(
                                startTime = formatTimeForCalculation(startTimeHours, startTimeMinutes),
                                endTime = formatTimeForCalculation(endTimeHours, endTimeMinutes),
                                rating = selectedRating,
                                timestamp = System.currentTimeMillis()
                            )
                            sleepRecords = sleepRecords + newRecord
                            saveSleepRecords(prefs, gson, sleepRecords, context)
                            saveSleepStatsToFile(context, gson, sleepRecords)
                            resetInputs {
                                selectedRating = 0
                                startTimeHours = ""
                                startTimeMinutes = ""
                                endTimeHours = ""
                                endTimeMinutes = ""
                                showResult = false
                                showErrors = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleepColors.Orange)
                ) {
                    Text("Добавить", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showResult && isValid) {
                item {
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Text(
                    text = "История сна",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleepColors.SemiTransparentWhite,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (sleepRecords.isNotEmpty()) {
                items(sleepRecords.reversed()) { record ->
                    val (duration, _) = calculateSleepDuration(record.startTime, record.endTime)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(record.timestamp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF494358), Color(0xFF3A3247))
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formattedDate,
                                    color = SleepColors.SemiTransparentWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Длительность: ${duration.format()}",
                                    color = SleepColors.LightGray,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Оценка: ${record.rating}★",
                                    color = SleepColors.LightGray,
                                    fontSize = 16.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Удалить запись",
                                tint = ExcessColor,
                                modifier = Modifier
                                    .clickable {
                                        sleepRecords = sleepRecords.filter { it != record }
                                        saveSleepRecords(prefs, gson, sleepRecords, context)
                                        saveSleepStatsToFile(context, gson, sleepRecords)
                                    }
                                    .size(24.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "История сна пуста",
                        color = SleepColors.LightGray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Компонент для ввода времени начала и конца сна.
 *
 * @param startTimeHours Часы начала сна.
 * @param startTimeMinutes Минуты начала сна.
 * @param endTimeHours Часы окончания сна.
 * @param endTimeMinutes Минуты окончания сна.
 * @param onStartHoursChange Callback для изменения часов начала сна.
 * @param onStartMinutesChange Callback для изменения минут начала сна.
 * @param onEndHoursChange Callback для изменения часов окончания сна.
 * @param onEndMinutesChange Callback для изменения минут окончания сна.
 * @param startHoursError Флаг ошибки для часов начала сна.
 * @param startMinutesError Флаг ошибки для минут начала сна.
 * @param endHoursError Флаг ошибки для часов окончания сна.
 * @param endMinutesError Флаг ошибки для минут окончания сна.
 */
@Composable
fun TimeInputSection(
    startTimeHours: String, startTimeMinutes: String,
    endTimeHours: String, endTimeMinutes: String,
    onStartHoursChange: (String) -> Unit,
    onStartMinutesChange: (String) -> Unit,
    onEndHoursChange: (String) -> Unit,
    onEndMinutesChange: (String) -> Unit,
    startHoursError: Boolean,
    startMinutesError: Boolean,
    endHoursError: Boolean,
    endMinutesError: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleepColors.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimeInputRow("Начало сна", startTimeHours, startTimeMinutes,
            onStartHoursChange, onStartMinutesChange, startHoursError, startMinutesError)
        Spacer(modifier = Modifier.height(16.dp))
        TimeInputRow("Конец сна", endTimeHours, endTimeMinutes,
            onEndHoursChange, onEndMinutesChange, endHoursError, endMinutesError)
    }
}

/**
 * Компонент для отображения кругового прогресса сна.
 *
 * @param sleepHours Форматированная строка с длительностью сна.
 * @param progress Прогресс сна (0..1).
 * @param excessProgress Превышение прогресса (0..1).
 * @param qualityScore Оценка качества сна (0..10).
 */
@Composable
fun CircularSleepProgress(
    sleepHours: String,
    progress: Float,
    excessProgress: Float,
    qualityScore: Int
) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            drawArc(
                color = SecondaryColor.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.linearGradient(listOf(AccentColor, Color(0xFFFF9352))),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (excessProgress > 0) {
                drawArc(
                    color = ExcessColor,
                    startAngle = -90f + progress * 360f,
                    sweepAngle = min(excessProgress * 360f, 360f - progress * 360f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(sleepHours, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("часы сна", color = Color.Gray, fontSize = 14.sp)
            if (qualityScore > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Качество: $qualityScore/10",
                    color = SleepColors.LightGray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Компонент для выбора рейтинга сна (1-5 звёзд).
 *
 * @param selectedRating Текущий выбранный рейтинг.
 * @param onRatingChange Callback для изменения рейтинга.
 */
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

/**
 * Компонент для ввода времени (часы и минуты).
 *
 * @param label Метка поля ввода.
 * @param hours Часы.
 * @param minutes Минуты.
 * @param onHoursChange Callback для изменения часов.
 * @param onMinutesChange Callback для изменения минут.
 * @param hoursError Флаг ошибки для часов.
 * @param minutesError Флаг ошибки для минут.
 */
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

/**
 * Компонент для ввода времени в текстовом поле.
 *
 * @param value Текущее значение.
 * @param onValueChange Callback для изменения значения.
 * @param placeholder Текст-заполнитель.
 * @param isError Флаг ошибки.
 */
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
                            if (isError) SleepColors.ErrorRed.copy(alpha = 0.2f) else Color(0xFF494358)
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

/**
 * Преобразует часы и минуты в общее количество минут с начала суток.
 *
 * @param hours Часы.
 * @param minutes Минуты.
 * @return Количество минут с начала суток.
 */
fun formatTimeForCalculation(hours: String, minutes: String): Long {
    val hour = hours.toLongOrNull() ?: 0L
    val minute = minutes.toLongOrNull() ?: 0L
    return hour * 60 + minute
}

/**
 * Вычисляет длительность сна между началом и концом.
 *
 * @param startTime Время начала сна в минутах с начала суток.
 * @param endTime Время окончания сна в минутах с начала суток.
 * @return Пара из [TimeDuration] и флага валидности.
 */
fun calculateSleepDuration(startTime: Long, endTime: Long): Pair<TimeDuration, Boolean> {
    val total = when {
        startTime == endTime -> 1440L
        endTime > startTime -> endTime - startTime
        else -> (1440 - startTime) + endTime
    }
    return TimeDuration(total).let { it to (total > 0) }
}

/**
 * Вычисляет оценку качества сна.
 *
 * @param duration Длительность сна.
 * @param userRating Оценка пользователя (1-5).
 * @return Оценка качества сна (1-10).
 */
fun calculateSleepQualityScore(duration: TimeDuration, userRating: Int): Int {
    return if (duration.totalMinutes >= 420) userRating * 2 else max(userRating * 2 - 1, 1)
}

/**
 * Класс данных для представления длительности времени в минутах.
 *
 * @param totalMinutes Общее количество минут.
 */
data class TimeDuration(val totalMinutes: Long) {
    /**
     * Форматирует длительность в строку вида "Xч YYм".
     *
     * @return Форматированная строка.
     */
    fun format(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}ч ${"%02d".format(minutes)}м"
    }
}

/**
 * Генерирует имя файла на основе токена пользователя.
 *
 * @param baseName Базовое имя файла.
 * @param context Контекст приложения.
 * @return Имя файла с токеном.
 */
private fun getUserFileName(baseName: String, context: Context): String {
    val token = TokenManager.getToken(context) ?: "default_user"
    val sanitizedToken = token.replace("[^a-zA-Z0-9]".toRegex(), "_")
    return "${baseName}_${sanitizedToken}.json"
}

/**
 * Загружает записи о сне из файла.
 *
 * @param prefs SharedPreferences для хранения данных.
 * @param gson Объект Gson для десериализации.
 * @param context Контекст приложения.
 * @return Список записей о сне.
 */
private fun loadSleepRecords(prefs: android.content.SharedPreferences, gson: Gson, context: Context): List<SleepRecord> {
    val fileName = getUserFileName("sleep_records", context)
    val file = File(context.filesDir, fileName)
    if (!file.exists()) return emptyList()
    val json = file.readText()
    val type = object : TypeToken<List<SleepRecord>>() {}.type
    return gson.fromJson(json, type) ?: emptyList()
}

/**
 * Сохраняет записи о сне в файл.
 *
 * @param prefs SharedPreferences для хранения данных.
 * @param gson Объект Gson для сериализации.
 * @param records Список записей о сне.
 * @param context Контекст приложения.
 */
private fun saveSleepRecords(prefs: android.content.SharedPreferences, gson: Gson, records: List<SleepRecord>, context: Context) {
    val fileName = getUserFileName("sleep_records", context)
    val file = File(context.filesDir, fileName)
    val json = gson.toJson(records)
    file.writeText(json)
}

/**
 * Сохраняет статистику сна в файл.
 *
 * @param context Контекст приложения.
 * @param gson Объект Gson для сериализации.
 * @param records Список записей о сне.
 */
private fun saveSleepStatsToFile(context: Context, gson: Gson, records: List<SleepRecord>) {
    val stats = records.map { record ->
        val (duration, _) = calculateSleepDuration(record.startTime, record.endTime)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        mapOf(
            "date" to dateFormat.format(Date(record.timestamp)),
            "duration_minutes" to duration.totalMinutes,
            "rating" to record.rating,
            "quality_score" to calculateSleepQualityScore(duration, record.rating),
            "start_time" to record.startTime,
            "end_time" to record.endTime
        )
    }
    val fileName = getUserFileName("sleep_stats", context)
    val file = File(context.filesDir, fileName)
    val json = gson.toJson(stats)
    file.writeText(json)
}

/**
 * Проверяет валидность введённых данных.
 *
 * @param rating Оценка сна.
 * @param startHours Часы начала сна.
 * @param startMinutes Минуты начала сна.
 * @param endHours Часы окончания сна.
 * @param endMinutes Минуты окончания сна.
 * @param startHoursError Флаг ошибки для часов начала сна.
 * @param startMinutesError Флаг ошибки для минут начала сна.
 * @param endHoursError Флаг ошибки для часов окончания сна.
 * @param endMinutesError Флаг ошибки для минут окончания сна.
 * @return `true`, если данные валидны, иначе `false`.
 */
private fun isInputValid(
    rating: Int, startHours: String, startMinutes: String,
    endHours: String, endMinutes: String,
    startHoursError: Boolean, startMinutesError: Boolean,
    endHoursError: Boolean, endMinutesError: Boolean
) = rating > 0 &&
        startHours.isNotEmpty() && startMinutes.isNotEmpty() &&
        endHours.isNotEmpty() && endMinutes.isNotEmpty() &&
        !startHoursError && !startMinutesError &&
        !endHoursError && !endMinutesError

/**
 * Сбрасывает введённые данные.
 *
 * @param block Блок кода для сброса.
 */
private fun resetInputs(block: () -> Unit) = block()