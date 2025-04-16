package com.example.myapplication.registration

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.profile.ProfileViewModel
import com.example.myapplication.utils.TokenManager

@Composable
fun RegistrationScreen(
    navController: NavController,
    registrationViewModel: RegistrationViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var goalWeight by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var goalWeightError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    val genderOptions = listOf("Мужской", "Женский")
    var selectedGender by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Добро пожаловать!",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.inversePrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        RegistrationInputField(
            value = email,
            onValueChange = {
                val filtered = it.filter { char ->
                    char.isLetterOrDigit() || char in "@._-+"
                }
                email = filtered
                emailError = validateEmail(filtered, context)
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = emailError
        )

        RegistrationInputField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it, context)
            },
            label = "Пароль",
            keyboardType = KeyboardType.Password,
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = passwordError
        )

        RegistrationInputField(
            value = name,
            onValueChange = {
                name = it
                nameError = validateName(it, context)
            },
            label = "Имя",
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = nameError
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            DropdownMenuBox(
                options = genderOptions,
                selected = selectedGender,
                onSelectionChange = {
                    selectedGender = it
                    genderError = null
                },
                errorMessage = genderError
            )
        }
        RegistrationInputField(
            value = age,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() }
                age = filtered
                ageError = validateAge(filtered, context)
            },
            label = "Возраст, лет",
            keyboardType = KeyboardType.Number,
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = ageError
        )
        RegistrationInputField(
            value = weight,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() || char == '.' }
                weight = filtered
                weightError = validateWeight(filtered, context)
            },
            label = "Вес, кг",
            keyboardType = KeyboardType.Number,
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = weightError
        )
        RegistrationInputField(
            value = height,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() || char == '.' }
                height = filtered
                heightError = validateHeight(filtered, context)
            },
            label = "Рост, см",
            keyboardType = KeyboardType.Number,
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = heightError
        )
        RegistrationInputField(
            value = goalWeight,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() || char == '.' }
                goalWeight = filtered
                goalWeightError = validateGoalWeight(filtered, context)
            },
            label = "Цель, кг",
            keyboardType = KeyboardType.Number,
            textStyle = TextStyle(color = Color.White, fontSize = 19.sp),
            errorMessage = goalWeightError
        )
        Button(
            onClick = {
                emailError = validateEmail(email, context)
                passwordError = validatePassword(password, context)
                nameError = validateName(name, context)
                ageError = validateAge(age, context)
                weightError = validateWeight(weight, context)
                heightError = validateHeight(height, context)
                goalWeightError = validateGoalWeight(goalWeight, context)
                genderError = if (selectedGender == null) "Выберите пол" else null

                if (emailError == null && passwordError == null && nameError == null &&
                    ageError == null && weightError == null && heightError == null &&
                    goalWeightError == null && genderError == null
                ) {
                    registrationViewModel.registerUser(
                        username = email,
                        password = password,
                        name = name,
                        height = height.toDoubleOrNull() ?: 0.0,
                        weight = weight.toDoubleOrNull() ?: 0.0,
                        gender = selectedGender ?: "",
                        goalWeight = goalWeight.toDoubleOrNull() ?: 0.0,
                        onSuccess = { token ->
                            if (token.isNotEmpty()) {
                                TokenManager.saveToken(context, token)
                                Toast.makeText(context, context.getString(R.string.registration_success), Toast.LENGTH_LONG).show()
                                profileViewModel.fetchUserInfo(context)
                                navController.navigate("home") {
                                    popUpTo("registration") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.token_empty_error), Toast.LENGTH_LONG).show()
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, context.getString(R.string.registration_error, error), Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Зарегистрироваться", color = MaterialTheme.colorScheme.inversePrimary)
        }
        TextButton(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Уже есть аккаунт? Войти",
                color = MaterialTheme.colorScheme.inversePrimary,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun RegistrationInputField(
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
            label = { Text(text = label, color = MaterialTheme.colorScheme.inversePrimary) },
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = errorMessage != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    options: List<String>,
    selected: String?,
    onSelectionChange: (String) -> Unit,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Box(
            modifier = Modifier
                .border(0.5.dp, if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inversePrimary)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { expanded = true }) {
                Text(
                    text = selected ?: "Выберите пол",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 19.sp) },
                        onClick = {
                            onSelectionChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

fun validateEmail(email: String, context: Context): String? {
    return when {
        email.isBlank() -> context.getString(R.string.email_empty_error)
        email.contains("[А-Яа-я]".toRegex()) -> context.getString(R.string.email_cyrillic_error)
        !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()) -> context.getString(R.string.email_invalid_error)
        else -> null
    }
}

fun validatePassword(password: String, context: Context): String? {
    return when {
        password.isBlank() -> context.getString(R.string.password_empty_error)
        password.length < 8 -> context.getString(R.string.password_short_error)
        !password.any { it.isDigit() } -> context.getString(R.string.password_no_digit_error)
        !password.any { it.isLetter() } -> context.getString(R.string.password_no_letter_error)
        else -> null
    }
}

fun validateName(name: String, context: Context): String? {
    return when {
        name.isBlank() -> context.getString(R.string.name_empty_error)
        name.length < 2 -> context.getString(R.string.name_short_error)
        !name.matches("^[A-Za-zА-Яа-я]+$".toRegex()) -> context.getString(R.string.name_invalid_error)
        else -> null
    }
}

fun validateAge(age: String, context: Context): String? {
    val ageInt = age.toIntOrNull()
    return when {
        age.isBlank() -> context.getString(R.string.age_empty_error)
        ageInt == null -> context.getString(R.string.age_invalid_error)
        ageInt !in 1..120 -> context.getString(R.string.age_range_error)
        else -> null
    }
}

fun validateWeight(weight: String, context: Context): String? {
    val weightDouble = weight.toDoubleOrNull()
    return when {
        weight.isBlank() -> context.getString(R.string.weight_empty_error)
        weightDouble == null -> context.getString(R.string.weight_invalid_error)
        weightDouble !in 20.0..300.0 -> context.getString(R.string.weight_range_error)
        else -> null
    }
}

fun validateHeight(height: String, context: Context): String? {
    val heightDouble = height.toDoubleOrNull()
    return when {
        height.isBlank() -> context.getString(R.string.height_empty_error)
        heightDouble == null -> context.getString(R.string.height_invalid_error)
        heightDouble !in 50.0..250.0 -> context.getString(R.string.height_range_error)
        else -> null
    }
}

fun validateGoalWeight(goalWeight: String, context: Context): String? {
    val goalWeightDouble = goalWeight.toDoubleOrNull()
    return when {
        goalWeight.isBlank() -> context.getString(R.string.goal_weight_empty_error)
        goalWeightDouble == null -> context.getString(R.string.goal_weight_invalid_error)
        goalWeightDouble !in 20.0..300.0 -> context.getString(R.string.goal_weight_range_error)
        else -> null
    }
}