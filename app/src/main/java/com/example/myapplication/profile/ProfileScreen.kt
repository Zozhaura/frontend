package com.example.myapplication.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.utils.TokenManager
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Объект, содержащий цвета, используемые в приложении.
 */
object AppColors {
    val DarkBackground = Color(0xFF494358)
    val DarkCard = Color(0xFF3A3A3A)
    val OrangeAccent = Color(0xFFFFA500)
    val LightGrayBorder = Color(0xFFD1C4E9)
    val SemiTransparentWhite = Color(0x9AFFFFFF)
    val LoadingColor = Color(0xFF9575CD)
}

/**
 * Объект, содержащий размеры элементов интерфейса.
 */
object AppDimens {
    val ProfileImageSize = 150.dp
    val InputFontSize = 15.sp
    val ButtonPadding = 2.dp
    val CardPadding = 2.dp
    val CornerRadius = 16.dp
}

/**
 * Экран профиля пользователя.
 *
 * Позволяет пользователю просматривать и редактировать свои данные, такие как имя, возраст, рост, вес, целевой вес и аватар.
 *
 * @param navController Контроллер навигации.
 * @param viewModel ViewModel для управления данными профиля.
 * @param showBackButton Флаг, указывающий, нужно ли отображать кнопку "Назад".
 * @param backRoute Маршрут для возврата при нажатии кнопки "Назад".
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(),
    showBackButton: Boolean = false,
    backRoute: String? = null
) {
    val context = LocalContext.current
    val userResponse by viewModel.userResponse
    val errorMessage by viewModel.errorMessage
    val isLoading by viewModel.isLoading
    val isUpdating by viewModel.isUpdating
    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var goalWeightError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { avatarUri = it }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUserInfo(context)
    }

    LaunchedEffect(userResponse) {
        userResponse?.let {
            name = it.name
            height = it.height.toString()
            weight = it.weight.toString()
            goalWeight = it.goalWeight.toString()
            email = it.username
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.contains("Токен отсутствует") || it.contains("Ошибка авторизации")) {
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            }
        }
    }

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
                        popUpTo("profile") { inclusive = true }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад на главный экран",
                    tint = AppColors.SemiTransparentWhite
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DarkBackground)
        ) {
            if (isLoading && userResponse == null) {
                CircularProgressIndicator(
                    color = AppColors.LoadingColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppDimens.ButtonPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        if (showBackButton) {
                            IconButton(onClick = {
                                if (backRoute != null) {
                                    navController.navigate(backRoute)
                                } else {
                                    navController.popBackStack()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = stringResource(R.string.back_button_description),
                                    tint = Color.White
                                )
                            }
                        }

                        ProfileImageSection(
                            avatarUri = avatarUri,
                            onEditClick = { pickImageLauncher.launch("image/*") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.CardPadding),
                            shape = RoundedCornerShape(AppDimens.CornerRadius),
                            color = AppColors.DarkCard
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProfileText(
                                    text = if (name.isNotBlank()) name else "Имя не указано",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.CardPadding),
                            shape = RoundedCornerShape(AppDimens.CornerRadius),
                            color = AppColors.DarkCard
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                ProfileInputField(
                                    value = name,
                                    onValueChange = {
                                        val filtered = it.filter { char -> char.isLetter() || char.isWhitespace() }
                                        name = filtered
                                        nameError = validateName(filtered, context)
                                    },
                                    label = "Имя",
                                    textStyle = TextStyle(color = Color.White, fontSize = AppDimens.InputFontSize),
                                    errorMessage = nameError
                                )
                                ProfileInputField(
                                    value = height,
                                    onValueChange = {
                                        val filtered = it.filter { char -> char.isDigit() || char == '.' }
                                        height = filtered
                                        heightError = validateHeight(filtered, context)
                                    },
                                    label = "Рост (см)",
                                    keyboardType = KeyboardType.Number,
                                    textStyle = TextStyle(color = Color.White, fontSize = AppDimens.InputFontSize),
                                    errorMessage = heightError
                                )
                                ProfileInputField(
                                    value = weight,
                                    onValueChange = {
                                        val filtered = it.filter { char -> char.isDigit() || char == '.' }
                                        weight = filtered
                                        weightError = validateWeight(filtered, context)
                                    },
                                    label = "Вес (кг)",
                                    keyboardType = KeyboardType.Number,
                                    textStyle = TextStyle(color = Color.White, fontSize = AppDimens.InputFontSize),
                                    errorMessage = weightError
                                )
                                ProfileInputField(
                                    value = goalWeight,
                                    onValueChange = {
                                        val filtered = it.filter { char -> char.isDigit() || char == '.' }
                                        goalWeight = filtered
                                        goalWeightError = validateGoalWeight(filtered, context)
                                    },
                                    label = "Цель веса (кг)",
                                    keyboardType = KeyboardType.Number,
                                    textStyle = TextStyle(color = Color.White, fontSize = AppDimens.InputFontSize),
                                    errorMessage = goalWeightError
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                    }

                    item {
                        Button(
                            onClick = {
                                nameError = validateName(name, context)
                                emailError = validateEmail(email, context)
                                heightError = validateHeight(height, context)
                                weightError = validateWeight(weight, context)
                                goalWeightError = validateGoalWeight(goalWeight, context)

                                if (nameError == null && emailError == null && heightError == null && weightError == null && goalWeightError == null) {
                                    viewModel.updateUserInfo(
                                        context = context,
                                        name = name,
                                        height = height.toDoubleOrNull() ?: 0.0,
                                        weight = weight.toDoubleOrNull() ?: 0.0,
                                        goalWeight = goalWeight.toDoubleOrNull() ?: 0.0,
                                        username = email,
                                        avatarUri = avatarUri,
                                        onSuccess = {
                                            Toast.makeText(context, context.getString(R.string.profile_update_success), Toast.LENGTH_SHORT).show()
                                            saveWeightStats(context, weight.toDoubleOrNull() ?: 0.0)
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, context.getString(R.string.profile_update_error, error), Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.CardPadding, vertical = AppDimens.ButtonPadding)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent),
                            shape = RoundedCornerShape(AppDimens.CornerRadius),
                            enabled = !isUpdating
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AppColors.SemiTransparentWhite
                                )
                            } else {
                                Text(
                                    text = "Сохранить",
                                    color = AppColors.SemiTransparentWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Button(
                            onClick = {
                                TokenManager.clearToken(context)
                                Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.CardPadding, vertical = AppDimens.ButtonPadding)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent),
                            shape = RoundedCornerShape(AppDimens.CornerRadius)
                        ) {
                            Text(
                                text = "Выйти из аккаунта",
                                color = AppColors.SemiTransparentWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

/**
 * Компонент для отображения и редактирования изображения профиля.
 *
 * @param avatarUri URI аватара.
 * @param onEditClick Callback для редактирования аватара.
 */
@Composable
fun ProfileImageSection(avatarUri: Uri?, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(AppDimens.ProfileImageSize)
            .clip(CircleShape)
            .background(AppColors.DarkCard)
            .border(2.dp, AppColors.OrangeAccent, CircleShape)
    ) {
        Image(
            painter = if (avatarUri != null) {
                rememberAsyncImagePainter(avatarUri)
            } else {
                painterResource(id = R.drawable.cat_image)
            },
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((-16).dp, (-16).dp)
                .size(20.dp)
                .background(AppColors.OrangeAccent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Avatar",
                tint = AppColors.SemiTransparentWhite
            )
        }
    }
}

/**
 * Компонент для отображения текста в профиле.
 *
 * @param text Текст для отображения.
 * @param fontSize Размер шрифта.
 * @param fontWeight Толщина шрифта (по умолчанию FontWeight.Normal).
 */
@Composable
fun ProfileText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        color = AppColors.SemiTransparentWhite,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

/**
 * Компонент для ввода данных профиля.
 *
 * @param value Текущее значение поля ввода.
 * @param onValueChange Callback для изменения значения.
 * @param label Метка поля.
 * @param keyboardType Тип клавиатуры (по умолчанию KeyboardType.Text).
 * @param textStyle Стиль текста.
 * @param errorMessage Сообщение об ошибке (опционально).
 */
@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    textStyle: TextStyle = LocalTextStyle.current,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label, color = AppColors.SemiTransparentWhite) },
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppDimens.CardPadding),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.OrangeAccent,
                unfocusedBorderColor = AppColors.LightGrayBorder,
                errorBorderColor = Color.Red
            ),
            isError = errorMessage != null
        )
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 10.dp, top = 2.dp)
                )
            }
        }
    }
}

/**
 * Валидирует имя пользователя.
 *
 * @param name Имя для валидации.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validateName(name: String, context: Context): String? {
    return when {
        name.isBlank() -> context.getString(R.string.name_empty_error)
        name.length < 2 -> context.getString(R.string.name_short_error)
        !name.matches("^[A-Za-zА-Яа-я\\s]+$".toRegex()) -> context.getString(R.string.name_invalid_error)
        name.length > 50 -> context.getString(R.string.name_too_long_error)
        else -> null
    }
}

/**
 * Валидирует email.
 *
 * @param email Email для валидации.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validateEmail(email: String, context: Context): String? {
    return when {
        email.isBlank() -> context.getString(R.string.email_empty_error)
        !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()) -> context.getString(R.string.email_invalid_error)
        email.length > 100 -> context.getString(R.string.email_too_long_error)
        else -> null
    }
}

/**
 * Валидирует рост.
 *
 * @param height Рост для валидации.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validateHeight(height: String, context: Context): String? {
    val heightDouble = height.toDoubleOrNull()
    return when {
        height.isBlank() -> context.getString(R.string.height_empty_error)
        heightDouble == null -> context.getString(R.string.height_invalid_error)
        heightDouble !in 50.0..250.0 -> context.getString(R.string.height_range_error)
        else -> null
    }
}

/**
 * Валидирует вес.
 *
 * @param weight Вес для валидации.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validateWeight(weight: String, context: Context): String? {
    val weightDouble = weight.toDoubleOrNull()
    return when {
        weight.isBlank() -> context.getString(R.string.weight_empty_error)
        weightDouble == null -> context.getString(R.string.weight_invalid_error)
        weightDouble !in 20.0..300.0 -> context.getString(R.string.weight_range_error)
        else -> null
    }
}

/**
 * Валидирует целевой вес.
 *
 * @param goalWeight Целевой вес для валидации.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validateGoalWeight(goalWeight: String, context: Context): String? {
    val goalWeightDouble = goalWeight.toDoubleOrNull()
    return when {
        goalWeight.isBlank() -> context.getString(R.string.goal_weight_empty_error)
        goalWeightDouble == null -> context.getString(R.string.goal_weight_invalid_error)
        goalWeightDouble !in 20.0..300.0 -> context.getString(R.string.goal_weight_range_error)
        else -> null
    }
}



/**
 * Генерирует имя файла на основе токена пользователя.
 *
 * @param baseName Базовое имя файла.
 * @param context Контекст приложения.
 * @return Сгенерированное имя файла.
 */
private fun getUserFileName(baseName: String, context: Context): String {
    val token = TokenManager.getToken(context) ?: "default_user"
    val sanitizedToken = token.replace("[^a-zA-Z0-9]".toRegex(), "_")
    return "${baseName}_${sanitizedToken}.json"
}

/**
 * Сохраняет статистику веса в файл.
 *
 * @param context Контекст приложения.
 * @param weight Вес для сохранения.
 */
private fun saveWeightStats(context: Context, weight: Double) {
    val gson = Gson()
    val fileName = getUserFileName("weight_stats", context)
    val file = File(context.filesDir, fileName)

    val existingStats = if (file.exists()) {
        val json = file.readText()
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
        gson.fromJson(json, type) ?: mutableListOf<Map<String, Any>>()
    } else {
        mutableListOf<Map<String, Any>>()
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val newRecord = mapOf(
        "date" to dateFormat.format(Date()),
        "weight" to weight
    )

    val updatedStats = existingStats.toMutableList().apply {
        add(newRecord)
    }

    val json = gson.toJson(updatedStats)
    file.writeText(json)
}

/**
 * Загружает статистику веса из файла.
 *
 * @param context Контекст приложения.
 * @return Список записей статистики веса.
 */
fun loadWeightStats(context: Context): List<Map<String, Any>> {
    val gson = Gson()
    val fileName = getUserFileName("weight_stats", context)
    val file = File(context.filesDir, fileName)

    return if (file.exists()) {
        val json = file.readText()
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    } else {
        emptyList()
    }
}