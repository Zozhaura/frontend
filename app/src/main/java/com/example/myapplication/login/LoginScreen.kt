package com.example.myapplication.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.myapplication.profile.ProfileViewModel
import com.example.myapplication.utils.TokenManager

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.inversePrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LoginInputField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        LoginInputField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            keyboardType = KeyboardType.Password
        )

        Button(
            onClick = {
                loginViewModel.loginUser(
                    username = email,
                    password = password,
                    onSuccess = { token ->
                        TokenManager.saveToken(context, token)
                        Toast.makeText(context, "Вход успешен", Toast.LENGTH_SHORT).show()
                        profileViewModel.fetchUserInfo(context)
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onError = { error ->
                        Toast.makeText(context, "Ошибка входа: $error", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Войти", color = MaterialTheme.colorScheme.inversePrimary)
        }

        TextButton(
            onClick = { navController.navigate("registration") },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "Нет аккаунта? Зарегистрироваться",
                color = MaterialTheme.colorScheme.inversePrimary,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = MaterialTheme.colorScheme.inversePrimary) },
        textStyle = TextStyle(color = Color.LightGray, fontSize = 19.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}