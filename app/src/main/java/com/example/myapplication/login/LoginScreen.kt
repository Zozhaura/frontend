package com.example.myapplication.login

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.profile.ProfileViewModel
import com.example.myapplication.utils.TokenManager

/**
 * Экран входа в приложение.
 *
 * Позволяет пользователю ввести email и пароль, выполнить вход и перейти к регистрации.
 *
 * @param navController Контроллер навигации.
 * @param loginViewModel ViewModel для управления процессом входа.
 * @param profileViewModel ViewModel для загрузки информации о пользователе.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val isLoading by loginViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_title),
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LoginInputField(
            value = email,
            onValueChange = {
                val filtered = it.filter { char ->
                    char.isLetterOrDigit() || char in "@._-+"
                }
                if (filtered != email) {
                    email = filtered
                }
                emailError = validateEmail(filtered, context)
            },
            label = stringResource(R.string.email_label),
            keyboardType = KeyboardType.Email,
            errorMessage = emailError,
            textStyle = TextStyle(color = Color.White)
        )
        LoginInputField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it, context)
            },
            label = stringResource(R.string.password_label),
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = stringResource(R.string.toggle_password_visibility)
                    )
                }
            },
            errorMessage = passwordError,
            textStyle = TextStyle(color = Color.White)
        )
        Button(
            onClick = {
                emailError = validateEmail(email, context)
                passwordError = validatePassword(password, context)

                if (emailError == null && passwordError == null) {
                    if (isNetworkAvailable(context)) {
                        loginViewModel.loginUser(
                            username = email,
                            password = password,
                            onSuccess = { token ->
                                TokenManager.saveToken(context, token)
                                Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                                profileViewModel.fetchUserInfo(context)
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onError = { error ->
                                Toast.makeText(context, context.getString(R.string.login_error, error), Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, context.getString(R.string.no_internet), Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.login_button))
            }
        }

        TextButton(
            onClick = { navController.navigate("registration") },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.register_prompt),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Компонент ввода данных для экрана входа.
 *
 * Используется для ввода email и пароля с валидацией и отображением ошибок.
 *
 * @param value Текущее значение поля ввода.
 * @param onValueChange Callback для изменения значения.
 * @param label Метка поля ввода.
 * @param keyboardType Тип клавиатуры (по умолчанию KeyboardType.Text).
 * @param visualTransformation Трансформация ввода (например, для пароля).
 * @param trailingIcon Иконка в конце поля (опционально).
 * @param errorMessage Сообщение об ошибке (опционально).
 * @param textStyle Стиль текста.
 */
@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String? = null,
    textStyle: TextStyle = LocalTextStyle.current
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            isError = errorMessage != null,
            textStyle = textStyle,
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

/**
 * Валидирует email пользователя.
 *
 * Проверяет, что email не пустой, не содержит кириллицу и соответствует формату email.
 *
 * @param email Введённый email.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validateEmail(email: String, context: Context): String? {
    return when {
        email.isBlank() -> context.getString(R.string.email_empty_error)
        email.contains("[А-Яа-я]".toRegex()) -> context.getString(R.string.email_cyrillic_error)
        !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()) -> context.getString(R.string.email_invalid_error)
        else -> null
    }
}

/**
 * Валидирует пароль пользователя.
 *
 * Проверяет, что пароль не пустой, содержит минимум 8 символов, буквы и цифры.
 *
 * @param password Введённый пароль.
 * @param context Контекст приложения.
 * @return Сообщение об ошибке или null, если валидация прошла успешно.
 */
fun validatePassword(password: String, context: Context): String? {
    return when {
        password.isBlank() -> context.getString(R.string.password_empty_error)
        password.length < 8 -> context.getString(R.string.password_short_error)
        !password.any { it.isDigit() } -> context.getString(R.string.password_no_digit_error)
        !password.any { it.isLetter() } -> context.getString(R.string.password_no_letter_error)
        else -> null
    }
}

/**
 * Проверяет доступность сети.
 *
 * @param context Контекст приложения.
 * @return true, если сеть доступна, иначе false.
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
    val network = connectivityManager?.activeNetwork
    return network != null
}