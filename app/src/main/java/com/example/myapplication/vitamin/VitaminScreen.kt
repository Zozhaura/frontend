package com.example.myapplication.vitamin

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

/**
 * Объект, содержащий цвета, используемые на экране витаминов.
 */
object VitaminColors {
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val ProgressBarColor = Color(0xFFBB86FC)
    val VitaminABackground = Color(0xFFFF6F61)
    val VitaminB1Background = Color(0xFF6B5B95)
    val VitaminB2Background = Color(0xFF88B04B)
    val VitaminB3Background = Color(0xFF705757)
    val VitaminB5Background = Color(0xFF9575CD)
    val VitaminB6Background = Color(0xFF4CAF50)
    val VitaminB12Background = Color(0xFF42A5F5)
    val VitaminCBackground = Color(0xFFFFCA28)
    val VitaminDBackground = Color(0xFFEF5350)
    val VitaminEBackground = Color(0xFFAB47BC)
    val VitaminKBackground = Color(0xFF26A69A)
    val CustomVitaminBackground = Color(0xFF78909C)
    val InputFieldBackground = Color(0xFF3A3A3A)
    val TakenColor = Color(0xFF4CAF50)
    val NotTakenColor = Color(0xFFE53935)
    val ButtonBackground = Color(0xFF6200EE)
}

/**
 * Объект, содержащий размеры, используемые на экране витаминов.
 */
object VitaminDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val CardCornerRadius = 16.dp
    val CardElevation = 4.dp
    val ProgressBarSize = 60.dp
    val VitaminBoxSize = 60.dp
    val ButtonHeight = 36.dp
    val DayCircleSize = 36.dp
}

/**
 * Перечисление единиц дозировки витаминов.
 */
enum class DosageUnit {
    MG, G
}

/**
 * Класс данных для хранения информации о витамине.
 *
 * @param name Название витамина.
 * @param dosage Количество приёмов в день.
 * @param dosageAmount Дозировка на один приём.
 * @param dosageUnit Единица измерения дозировки.
 * @param color Цвет, ассоциированный с витамином.
 * @param isCustom Флаг, указывающий, является ли витамин пользовательским.
 */
data class VitaminData(
    val name: String,
    var dosage: Int,
    var dosageAmount: Float,
    var dosageUnit: DosageUnit,
    val color: Color,
    val isCustom: Boolean = false
)

/**
 * Экран для управления приёмом витаминов.
 *
 * Позволяет пользователю добавлять витамины, отмечать их приём и отслеживать прогресс за неделю.
 *
 * @param navController Контроллер навигации для перехода между экранами.
 */
@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VitaminScreen(
    navController: NavController
) {
    val initialVitamins = listOf(
        VitaminData("A", 2, 500f, DosageUnit.MG, VitaminColors.VitaminABackground),
        VitaminData("B1", 1, 1.2f, DosageUnit.MG, VitaminColors.VitaminB1Background),
        VitaminData("B2", 1, 1.3f, DosageUnit.MG, VitaminColors.VitaminB2Background),
        VitaminData("B3", 1, 16f, DosageUnit.MG, VitaminColors.VitaminB3Background),
        VitaminData("B5", 2, 5f, DosageUnit.MG, VitaminColors.VitaminB5Background),
        VitaminData("B6", 1, 1.7f, DosageUnit.MG, VitaminColors.VitaminB6Background),
        VitaminData("B12", 1, 2.4f, DosageUnit.MG, VitaminColors.VitaminB12Background),
        VitaminData("C", 1, 90f, DosageUnit.MG, VitaminColors.VitaminCBackground),
        VitaminData("D", 1, 600f, DosageUnit.MG, VitaminColors.VitaminDBackground),
        VitaminData("E", 1, 15f, DosageUnit.MG, VitaminColors.VitaminEBackground),
        VitaminData("K", 1, 120f, DosageUnit.MG, VitaminColors.VitaminKBackground)
    )

    val vitamins = remember { mutableStateListOf<VitaminData>().apply { addAll(initialVitamins) } }
    val intakeRecordsMap: SnapshotStateMap<VitaminData, SnapshotStateMap<String, SnapshotStateList<LocalDate>>> =
        remember { mutableStateMapOf() }
    vitamins.forEach { vitamin ->
        if (!intakeRecordsMap.containsKey(vitamin)) {
            intakeRecordsMap[vitamin] = mutableStateMapOf()
        }
    }

    var showAddVitaminForm by remember { mutableStateOf(false) }
    var newVitaminName by remember { mutableStateOf("") }
    var newVitaminDosage by remember { mutableStateOf("") }
    var newVitaminDosageAmount by remember { mutableStateOf("") }
    var newVitaminDosageUnit by remember { mutableStateOf(DosageUnit.MG) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VitaminDimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("vitamin") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад на главный экран",
                    tint = VitaminColors.TextColor
                )
            }
            Text(
                text = "ВИТАМИНЫ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VitaminColors.TextColor
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VitaminDimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Button(
                    onClick = { showAddVitaminForm = !showAddVitaminForm },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VitaminDimens.ButtonHeight)
                        .padding(bottom = VitaminDimens.PaddingMedium),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VitaminColors.ButtonBackground)
                ) {
                    Text(
                        text = "Добавить свой витамин",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = showAddVitaminForm) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = VitaminDimens.PaddingMedium),
                        shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
                        color = VitaminColors.CardBackground,
                        shadowElevation = VitaminDimens.CardElevation
                    ) {
                        Column(
                            modifier = Modifier.padding(VitaminDimens.PaddingMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = newVitaminName,
                                    onValueChange = { newVitaminName = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(VitaminColors.InputFieldBackground, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = VitaminColors.TextColor,
                                        fontSize = 16.sp
                                    ),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (newVitaminName.isEmpty()) {
                                            Text(
                                                text = "Название витамина",
                                                color = Color.Gray,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = newVitaminDosage,
                                    onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) newVitaminDosage = it },
                                    modifier = Modifier
                                        .width(60.dp)
                                        .background(VitaminColors.InputFieldBackground, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = VitaminColors.TextColor,
                                        fontSize = 16.sp
                                    ),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (newVitaminDosage.isEmpty()) {
                                            Text(
                                                text = "Доза",
                                                color = Color.Gray,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = newVitaminDosageAmount,
                                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' } && it.length <= 6) newVitaminDosageAmount = it },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .background(VitaminColors.InputFieldBackground, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = VitaminColors.TextColor,
                                        fontSize = 16.sp
                                    ),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (newVitaminDosageAmount.isEmpty()) {
                                            Text(
                                                text = "Кол-во",
                                                color = Color.Gray,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { newVitaminDosageUnit = DosageUnit.MG },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (newVitaminDosageUnit == DosageUnit.MG)
                                            VitaminColors.ProgressBarColor else VitaminColors.InputFieldBackground
                                    )
                                ) {
                                    Text(
                                        text = "мг",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { newVitaminDosageUnit = DosageUnit.G },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (newVitaminDosageUnit == DosageUnit.G)
                                            VitaminColors.ProgressBarColor else VitaminColors.InputFieldBackground
                                    )
                                ) {
                                    Text(
                                        text = "г",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val dosage = newVitaminDosage.toIntOrNull() ?: 0
                                    val dosageAmount = newVitaminDosageAmount.toFloatOrNull() ?: 0f
                                    if (newVitaminName.isNotBlank() && dosage > 0 && dosageAmount > 0) {
                                        val newVitamin = VitaminData(
                                            name = newVitaminName,
                                            dosage = dosage,
                                            dosageAmount = dosageAmount,
                                            dosageUnit = newVitaminDosageUnit,
                                            color = VitaminColors.CustomVitaminBackground,
                                            isCustom = true
                                        )
                                        vitamins.add(newVitamin)
                                        intakeRecordsMap[newVitamin] = mutableStateMapOf()
                                        newVitaminName = ""
                                        newVitaminDosage = ""
                                        newVitaminDosageAmount = ""
                                        newVitaminDosageUnit = DosageUnit.MG
                                        showAddVitaminForm = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(VitaminDimens.ButtonHeight),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VitaminColors.ButtonBackground)
                            ) {
                                Text(
                                    text = "Добавить",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            items(vitamins.size) { index ->
                VitaminItem(
                    vitamin = vitamins[index],
                    intakeRecords = intakeRecordsMap[vitamins[index]] ?: mutableStateMapOf(),
                    onDosageChange = { newDosage, newDosageAmount, newDosageUnit ->
                        vitamins[index] = vitamins[index].copy(
                            dosage = newDosage,
                            dosageAmount = newDosageAmount,
                            dosageUnit = newDosageUnit
                        )
                    },
                    onDeleteVitamin = if (vitamins[index].isCustom) {
                        {
                            intakeRecordsMap.remove(vitamins[index])
                            vitamins.removeAt(index)
                        }
                    } else null
                )
            }

            item {
                Button(
                    onClick = {
                        intakeRecordsMap.clear()
                        vitamins.forEach { vitamin ->
                            intakeRecordsMap[vitamin] = mutableStateMapOf()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VitaminDimens.ButtonHeight)
                        .padding(top = VitaminDimens.PaddingMedium, bottom = VitaminDimens.PaddingMedium),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VitaminColors.ButtonBackground)
                ) {
                    Text(
                        text = "Выполнено",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Компонент для отображения информации о витамине и управления его приёмом.
 *
 * @param vitamin Данные о витамине.
 * @param intakeRecords Записи о приёмах витамина.
 * @param onDosageChange Callback для изменения дозировки.
 * @param onDeleteVitamin Callback для удаления витамина (опционально).
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VitaminItem(
    vitamin: VitaminData,
    intakeRecords: SnapshotStateMap<String, SnapshotStateList<LocalDate>>,
    onDosageChange: (Int, Float, DosageUnit) -> Unit,
    onDeleteVitamin: (() -> Unit)? = null
) {
    val currentDate = LocalDate.now()
    val startOfWeek = currentDate.minusDays(currentDate.dayOfWeek.value.toLong() - 1)
    val daysOfWeek = (0 until 7).map { startOfWeek.plusDays(it.toLong()) }

    val totalDoses = vitamin.dosage * 7
    val takenDoses by remember(intakeRecords) {
        derivedStateOf {
            daysOfWeek.sumOf { day ->
                intakeRecords[day.toString()]?.size?.toLong() ?: 0L
            }
        }
    }

    val progress by remember(takenDoses) {
        derivedStateOf {
            if (totalDoses > 0) (takenDoses.toFloat() / totalDoses).coerceIn(0f, 1f) else 0f
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editedDosage by remember { mutableStateOf(vitamin.dosage.toString()) }
    var editedDosageAmount by remember { mutableStateOf(vitamin.dosageAmount.toString()) }
    var editedDosageUnit by remember { mutableStateOf(vitamin.dosageUnit) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = VitaminDimens.PaddingSmall),
        shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
        color = VitaminColors.CardBackground,
        shadowElevation = VitaminDimens.CardElevation
    ) {
        Column(
            modifier = Modifier.padding(VitaminDimens.PaddingMedium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = if (isExpanded) VitaminDimens.PaddingSmall else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(VitaminDimens.VitaminBoxSize)
                            .background(vitamin.color, shape = RoundedCornerShape(VitaminDimens.CardCornerRadius)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vitamin.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VitaminColors.TextColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Дозировка: ${vitamin.dosage} раз в день",
                            fontSize = 14.sp,
                            color = VitaminColors.TextColor
                        )
                        Text(
                            text = "На приём: ${vitamin.dosageAmount} ${
                                if (vitamin.dosageUnit == DosageUnit.MG) "мг" else "г"
                            }",
                            fontSize = 14.sp,
                            color = VitaminColors.TextColor
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                    tint = VitaminColors.TextColor
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Прогресс: ${(progress * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = VitaminColors.TextColor
                        )
                        Row {
                            IconButton(
                                onClick = { isEditing = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Редактировать дозировку",
                                    tint = VitaminColors.TextColor
                                )
                            }
                            if (onDeleteVitamin != null) {
                                Button(
                                    onClick = onDeleteVitamin,
                                    modifier = Modifier.height(VitaminDimens.ButtonHeight),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VitaminColors.NotTakenColor)
                                ) {
                                    Text(
                                        text = "Удалить",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isEditing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = editedDosage,
                                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) editedDosage = it },
                                modifier = Modifier
                                    .width(60.dp)
                                    .background(VitaminColors.InputFieldBackground, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = VitaminColors.TextColor,
                                    fontSize = 16.sp
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (editedDosage.isEmpty()) {
                                        Text(
                                            text = "Доза",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = editedDosageAmount,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' } && it.length <= 6) editedDosageAmount = it },
                                modifier = Modifier
                                    .width(80.dp)
                                    .background(VitaminColors.InputFieldBackground, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = VitaminColors.TextColor,
                                    fontSize = 16.sp
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (editedDosageAmount.isEmpty()) {
                                        Text(
                                            text = "Кол-во",
                                            color = Color.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { editedDosageUnit = DosageUnit.MG },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editedDosageUnit == DosageUnit.MG)
                                        VitaminColors.ProgressBarColor else VitaminColors.InputFieldBackground
                                )
                            ) {
                                Text(
                                    text = "мг",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { editedDosageUnit = DosageUnit.G },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editedDosageUnit == DosageUnit.G)
                                        VitaminColors.ProgressBarColor else VitaminColors.InputFieldBackground
                                )
                            ) {
                                Text(
                                    text = "г",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val dosage = editedDosage.toIntOrNull() ?: 0
                                val dosageAmount = editedDosageAmount.toFloatOrNull() ?: 0f
                                if (dosage > 0 && dosageAmount > 0) {
                                    onDosageChange(dosage, dosageAmount, editedDosageUnit)
                                    isEditing = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(VitaminDimens.ButtonHeight),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VitaminColors.ButtonBackground)
                        ) {
                            Text(
                                text = "Сохранить",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .size(VitaminDimens.ProgressBarSize)
                            .align(Alignment.CenterHorizontally),
                        color = VitaminColors.ProgressBarColor,
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    daysOfWeek.forEach { day ->
                        val dayKey = day.toString()
                        val intakeList = intakeRecords.getOrPut(dayKey) { mutableStateListOf() }
                        val intakeCount by derivedStateOf { intakeList.size }
                        val maxDosesToday = vitamin.dosage
                        val isTaken = intakeCount >= maxDosesToday
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(VitaminDimens.DayCircleSize)
                                        .background(
                                            if (isTaken) VitaminColors.TakenColor else VitaminColors.NotTakenColor,
                                            shape = RoundedCornerShape(50)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()),
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Принято: $intakeCount из $maxDosesToday",
                                    fontSize = 14.sp,
                                    color = VitaminColors.TextColor
                                )
                            }
                            Button(
                                onClick = {
                                    if (!isTaken) {
                                        intakeList.add(day)
                                    }
                                },
                                modifier = Modifier
                                    .height(VitaminDimens.ButtonHeight)
                                    .width(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTaken)
                                        VitaminColors.InputFieldBackground else VitaminColors.ButtonBackground
                                ),
                                enabled = !isTaken
                            ) {
                                Text(
                                    text = "Принять",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            intakeRecords.clear()
                            daysOfWeek.forEach { day ->
                                intakeRecords[day.toString()] = mutableStateListOf()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(VitaminDimens.ButtonHeight),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VitaminColors.ButtonBackground)
                    ) {
                        Text(
                            text = "Выполнено",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}