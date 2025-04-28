package com.example.myapplication.vitamin

import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import java.time.LocalDate
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

/**
 * Объект, содержащий цвета, используемые на экране витаминов.
 */
object VitaminColors {
    val CardBackground = Color(0xFF3A3347)
    val TextColor = Color.LightGray
    val White = Color.White

    val AccentColor = Color(0xFF6650a4)
    val SecondaryBackground = Color(0xFF5E4D7A)
    val ErrorColor = Color(0xFFE53935)

    val VitaminABackground = Color(0xFFFF6F61)
    val VitaminB1Background = Color(0xFF7046FD)
    val VitaminB2Background = Color(0xFF88B04B)
    val VitaminB3Background = Color(0xFFFF8585)
    val VitaminB5Background = Color(0xFF9575CD)
    val VitaminB6Background = Color(0xFF4CAF50)
    val VitaminB12Background = Color(0xFF42A5F5)
    val VitaminCBackground = Color(0xFFFFCA28)
    val VitaminDBackground = Color(0xFFEF5350)
    val VitaminEBackground = Color(0xFFAB47BC)
    val VitaminKBackground = Color(0xFF26A69A)
    val CustomVitaminBackground = Color(0xFF009AFF)

    val ButtonBackground = Color(0xFF6650a4)
    val ButtonContent = Color.White
    val OutlinedButtonBorder = Color(0xFF6650a4)
}

/**
 * Объект, содержащий размеры, используемые на экране витаминов.
 */
object VitaminDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp

    val CardCornerRadius = 16.dp
    val CardElevation = 8.dp
    val CardPadding = 16.dp

    val IconSize = 24.dp
    val SmallIconSize = 18.dp
    val VitaminIconSize = 40.dp
    val VitaminIconInnerSize = 20.dp
    val ButtonHeight = 48.dp
    val AddButtonHeight = 50.dp
    val ProgressBarHeight = 20.dp
    val DayCircleSize = 32.dp

    val TextSmall = 12.sp
    val TextMedium = 14.sp
    val TextLarge = 16.sp
    val TextTitle = 18.sp
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VitaminDimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("vitamin") { inclusive = true }
                    }
                },
                modifier = Modifier.size(VitaminDimens.VitaminIconSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад на главный экран",
                    tint = VitaminColors.TextColor
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VitaminDimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                OutlinedButton(
                    onClick = { showAddVitaminForm = !showAddVitaminForm },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VitaminDimens.AddButtonHeight)
                        .padding(bottom = VitaminDimens.PaddingMedium),
                    shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
                    border = BorderStroke(1.dp, VitaminColors.OutlinedButtonBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = VitaminColors.AccentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add vitamin",
                        modifier = Modifier.size(VitaminDimens.SmallIconSize)
                    )
                    Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))
                    Text(
                        text = "Добавить свой витамин",
                        fontSize = VitaminDimens.TextLarge
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
                            modifier = Modifier.padding(VitaminDimens.CardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newVitaminName,
                                    onValueChange = { newVitaminName = it },
                                    modifier = Modifier
                                        .weight(1f),
                                    label = { Text("Название витамина", color = VitaminColors.TextColor) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = VitaminColors.SecondaryBackground,
                                        unfocusedContainerColor = VitaminColors.SecondaryBackground,
                                        focusedTextColor = VitaminColors.White,
                                        unfocusedTextColor = VitaminColors.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))
                                OutlinedTextField(
                                    value = newVitaminDosage,
                                    onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) newVitaminDosage = it },
                                    modifier = Modifier
                                        .width(80.dp),
                                    label = { Text("Доза", color = VitaminColors.TextColor) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = VitaminColors.SecondaryBackground,
                                        unfocusedContainerColor = VitaminColors.SecondaryBackground,
                                        focusedTextColor = VitaminColors.White,
                                        unfocusedTextColor = VitaminColors.White
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(VitaminDimens.PaddingSmall))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newVitaminDosageAmount,
                                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' } && it.length <= 6) newVitaminDosageAmount = it },
                                    modifier = Modifier
                                        .width(100.dp),
                                    label = { Text("Количество", color = VitaminColors.TextColor) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = VitaminColors.SecondaryBackground,
                                        unfocusedContainerColor = VitaminColors.SecondaryBackground,
                                        focusedTextColor = VitaminColors.White,
                                        unfocusedTextColor = VitaminColors.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))
                                FilterChip(
                                    selected = newVitaminDosageUnit == DosageUnit.MG,
                                    onClick = { newVitaminDosageUnit = DosageUnit.MG },
                                    label = { Text("мг", color = if (newVitaminDosageUnit == DosageUnit.MG) VitaminColors.White else VitaminColors.TextColor) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VitaminColors.AccentColor,
                                        selectedLabelColor = VitaminColors.White,
                                        containerColor = VitaminColors.SecondaryBackground,
                                        labelColor = VitaminColors.TextColor
                                    )
                                )
                                Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))
                                FilterChip(
                                    selected = newVitaminDosageUnit == DosageUnit.G,
                                    onClick = { newVitaminDosageUnit = DosageUnit.G },
                                    label = { Text("г", color = if (newVitaminDosageUnit == DosageUnit.G) VitaminColors.White else VitaminColors.TextColor) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VitaminColors.AccentColor,
                                        selectedLabelColor = VitaminColors.White,
                                        containerColor = VitaminColors.SecondaryBackground,
                                        labelColor = VitaminColors.TextColor
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(VitaminDimens.PaddingMedium))
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
                                shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VitaminColors.ButtonBackground,
                                    contentColor = VitaminColors.ButtonContent
                                )
                            ) {
                                Text(
                                    text = "Добавить",
                                    fontSize = VitaminDimens.TextLarge,
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
                Spacer(modifier = Modifier.height(VitaminDimens.PaddingSmall))
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
                        .padding(top = VitaminDimens.PaddingMedium, bottom = VitaminDimens.PaddingLarge),
                    shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitaminColors.ButtonBackground,
                        contentColor = VitaminColors.ButtonContent
                    )
                ) {
                    Text(
                        text = "Выполнено",
                        fontSize = VitaminDimens.TextLarge,
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
        color = VitaminColors.CardBackground,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = VitaminDimens.CardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VitaminDimens.PaddingMedium, vertical = VitaminDimens.PaddingSmall)
    ) {
        Column(modifier = Modifier.padding(VitaminDimens.CardPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = VitaminDimens.PaddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(VitaminDimens.VitaminIconSize)
                            .background(
                                color = vitamin.color.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .padding(VitaminDimens.PaddingSmall),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getVitaminIcon(vitamin.name),
                            contentDescription = vitamin.name,
                            tint = vitamin.color,
                            modifier = Modifier.size(VitaminDimens.VitaminIconInnerSize)
                        )
                    }
                    Spacer(modifier = Modifier.width(VitaminDimens.PaddingMedium))
                    Column {
                        Text(
                            text = "Витамин ${vitamin.name}",
                            color = VitaminColors.White,
                            fontSize = VitaminDimens.TextTitle,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${vitamin.dosageAmount} ${if (vitamin.dosageUnit == DosageUnit.MG) "мг" else "г"} · ${vitamin.dosage} раз/день",
                            color = VitaminColors.TextColor,
                            fontSize = VitaminDimens.TextMedium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = vitamin.color,
                            fontSize = VitaminDimens.TextLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${takenDoses.toInt()}/$totalDoses",
                            color = VitaminColors.TextColor,
                            fontSize = VitaminDimens.TextSmall
                        )
                    }

                    Spacer(modifier = Modifier.width(VitaminDimens.PaddingMedium))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = VitaminColors.White,
                        modifier = Modifier.size(VitaminDimens.IconSize)
                    )
                }
            }

            if (isExpanded) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VitaminDimens.ProgressBarHeight)
                        .padding(vertical = VitaminDimens.PaddingSmall),
                    color = vitamin.color,
                    trackColor = VitaminColors.SecondaryBackground,
                )
                WeekDaysView(
                    daysOfWeek = daysOfWeek,
                    intakeRecords = intakeRecords,
                    maxDoses = vitamin.dosage,
                    color = vitamin.color
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = VitaminDimens.PaddingMedium),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { isEditing = !isEditing },
                        modifier = Modifier.padding(end = VitaminDimens.PaddingSmall),
                        shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
                        border = BorderStroke(1.dp, vitamin.color),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = vitamin.color
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            modifier = Modifier.size(VitaminDimens.SmallIconSize)
                        )
                        Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))
                        Text("Редактировать")
                    }

                    if (onDeleteVitamin != null) {
                        OutlinedButton(
                            onClick = onDeleteVitamin,
                            shape = RoundedCornerShape(VitaminDimens.CardCornerRadius),
                            border = BorderStroke(1.dp, VitaminColors.ErrorColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = VitaminColors.ErrorColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                modifier = Modifier.size(VitaminDimens.SmallIconSize)
                            )
                            Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))
                            Text("Удалить")
                        }
                    }
                }

                if (isEditing) {
                    VitaminEditForm(
                        editedDosage = editedDosage,
                        editedDosageAmount = editedDosageAmount,
                        editedDosageUnit = editedDosageUnit,
                        onDosageChange = { editedDosage = it },
                        onDosageAmountChange = { editedDosageAmount = it },
                        onDosageUnitChange = { editedDosageUnit = it },
                        onSave = {
                            val dosage = editedDosage.toIntOrNull() ?: 0
                            val dosageAmount = editedDosageAmount.toFloatOrNull() ?: 0f
                            if (dosage > 0 && dosageAmount > 0) {
                                onDosageChange(dosage, dosageAmount, editedDosageUnit)
                                isEditing = false
                            }
                        },
                        onCancel = { isEditing = false }
                    )
                }
            }
        }
    }
}

/**
 * Отображает дни недели с индикаторами приёма витаминов.
 *
 * @param daysOfWeek Список дней недели.
 * @param intakeRecords Записи о приёмах.
 * @param maxDoses Максимальное количество приёмов в день.
 * @param color Цвет индикаторов.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekDaysView(
    daysOfWeek: List<LocalDate>,
    intakeRecords: SnapshotStateMap<String, SnapshotStateList<LocalDate>>,
    maxDoses: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = VitaminDimens.PaddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { day ->
            val dayKey = day.toString()
            val intakeList = intakeRecords.getOrPut(dayKey) { mutableStateListOf() }
            val intakeCount by derivedStateOf { intakeList.size }
            val isTaken = intakeCount >= maxDoses

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(VitaminDimens.DayCircleSize)
            ) {
                Text(
                    text = day.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()),
                    color = VitaminColors.TextColor,
                    fontSize = VitaminDimens.TextSmall
                )

                Spacer(modifier = Modifier.height(VitaminDimens.PaddingSmall))

                Box(
                    modifier = Modifier
                        .size(VitaminDimens.DayCircleSize)
                        .clip(CircleShape)
                        .background(
                            if (isTaken) color.copy(alpha = 0.2f)
                            else VitaminColors.SecondaryBackground
                        )
                        .border(
                            width = 1.dp,
                            color = if (isTaken) color else VitaminColors.AccentColor,
                            shape = CircleShape
                        )
                        .clickable {
                            if (!isTaken) {
                                intakeList.add(day)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTaken) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isTaken) "Принято" else "Принять",
                        tint = if (isTaken) color else VitaminColors.White,
                        modifier = Modifier.size(VitaminDimens.SmallIconSize)
                    )
                }
            }
        }
    }
}

/**
 * Возвращает иконку для соответствующего витамина.
 *
 * @param vitaminName Название витамина.
 * @return ImageVector соответствующей иконки.
 */
@Composable
fun getVitaminIcon(vitaminName: String): ImageVector {
    return when(vitaminName) {
        "A" -> Icons.Default.Lens
        "B1" -> Icons.Default.Bolt
        "B2" -> Icons.Default.FlashOn
        "B3" -> Icons.Default.Whatshot
        "B5" -> Icons.Default.Spa
        "B6" -> Icons.Default.Nature
        "B12" -> Icons.Default.Science
        "C" -> Icons.Default.LocalDrink
        "D" -> Icons.Default.WbSunny
        "E" -> Icons.Default.Spa
        "K" -> Icons.Default.Healing
        else -> Icons.Default.MedicalServices
    }
}

/**
 * Форма редактирования дозировки витамина.
 *
 * @param editedDosage Текущее количество приёмов.
 * @param editedDosageAmount Текущая дозировка.
 * @param editedDosageUnit Текущая единица измерения.
 * @param onDosageChange Обработчик изменения количества приёмов.
 * @param onDosageAmountChange Обработчик изменения дозировки.
 * @param onDosageUnitChange Обработчик изменения единицы измерения.
 * @param onSave Обработчик сохранения.
 * @param onCancel Обработчик отмены.
 */
@Composable
fun VitaminEditForm(
    editedDosage: String,
    editedDosageAmount: String,
    editedDosageUnit: DosageUnit,
    onDosageChange: (String) -> Unit,
    onDosageAmountChange: (String) -> Unit,
    onDosageUnitChange: (DosageUnit) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = VitaminDimens.PaddingMedium)
    ) {
        Text(
            text = "Редактировать дозировку",
            color = VitaminColors.White,
            fontSize = VitaminDimens.TextLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = VitaminDimens.PaddingSmall)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = editedDosage,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) onDosageChange(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Приемов в день", color = VitaminColors.TextColor) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = VitaminColors.SecondaryBackground,
                    unfocusedContainerColor = VitaminColors.SecondaryBackground,
                    focusedTextColor = VitaminColors.White,
                    unfocusedTextColor = VitaminColors.White
                )
            )

            Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))

            OutlinedTextField(
                value = editedDosageAmount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' } && it.length <= 6) onDosageAmountChange(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Дозировка", color = VitaminColors.TextColor) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = VitaminColors.SecondaryBackground,
                    unfocusedContainerColor = VitaminColors.SecondaryBackground,
                    focusedTextColor = VitaminColors.White,
                    unfocusedTextColor = VitaminColors.White
                )
            )
        }

        Spacer(modifier = Modifier.height(VitaminDimens.PaddingSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterChip(
                selected = editedDosageUnit == DosageUnit.MG,
                onClick = { onDosageUnitChange(DosageUnit.MG) },
                label = { Text("мг", color = if (editedDosageUnit == DosageUnit.MG) VitaminColors.White else VitaminColors.TextColor) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VitaminColors.AccentColor,
                    selectedLabelColor = VitaminColors.White,
                    containerColor = VitaminColors.SecondaryBackground,
                    labelColor = VitaminColors.TextColor
                )
            )

            Spacer(modifier = Modifier.width(VitaminDimens.PaddingSmall))

            FilterChip(
                selected = editedDosageUnit == DosageUnit.G,
                onClick = { onDosageUnitChange(DosageUnit.G) },
                label = { Text("г", color = if (editedDosageUnit == DosageUnit.G) VitaminColors.White else VitaminColors.TextColor) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VitaminColors.AccentColor,
                    selectedLabelColor = VitaminColors.White,
                    containerColor = VitaminColors.SecondaryBackground,
                    labelColor = VitaminColors.TextColor
                )
            )
        }
        Spacer(modifier = Modifier.height(VitaminDimens.PaddingMedium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.padding(end = VitaminDimens.PaddingSmall),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF6650a4)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF6650a4))
            ) {
                Text("Отмена")
            }

            Button(
                onClick = onSave,
                enabled = editedDosage.isNotBlank() && editedDosageAmount.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6650a4),
                    contentColor = Color.White
                )
            ) {
                Text("Сохранить")
            }
        }
    }
}