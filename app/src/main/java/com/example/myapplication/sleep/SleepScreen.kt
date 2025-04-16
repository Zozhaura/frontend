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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.max
import kotlin.math.min

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

data class SleepRecord(
    val startTime: Long,
    val endTime: Long,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun SleepScreen() {
    val context = LocalContext.current
    val gson = Gson()
    val prefs = context.getSharedPreferences("SleepPrefs", Context.MODE_PRIVATE)
    var sleepRecords by remember {
        mutableStateOf(loadSleepRecords(prefs, gson))
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
                        rating = selectedRating
                    )
                    sleepRecords = sleepRecords + newRecord
                    saveSleepRecords(prefs, gson, sleepRecords)
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

        SleepHistory(sleepRecords) { record ->
            sleepRecords = sleepRecords.filter { it != record }
            saveSleepRecords(prefs, gson, sleepRecords)
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

@Composable
fun SleepHistory(records: List<SleepRecord>, onDelete: (SleepRecord) -> Unit) {
    if (records.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(records.reversed()) { record ->
                val (duration, _) = calculateSleepDuration(record.startTime, record.endTime)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${duration.format()} (${record.rating}★)",
                        color = SleepColors.LightGray
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Delete",
                        tint = ExcessColor,
                        modifier = Modifier
                            .clickable { onDelete(record) }
                            .size(24.dp)
                    )
                }
            }
        }
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

private fun loadSleepRecords(prefs: android.content.SharedPreferences, gson: Gson): List<SleepRecord> {
    val json = prefs.getString("sleep_records", null) ?: return emptyList()
    val type = object : TypeToken<List<SleepRecord>>() {}.type
    return gson.fromJson(json, type)
}

private fun saveSleepRecords(prefs: android.content.SharedPreferences, gson: Gson, records: List<SleepRecord>) {
    with(prefs.edit()) {
        putString("sleep_records", gson.toJson(records))
        apply()
    }
}

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

private fun resetInputs(block: () -> Unit) = block()