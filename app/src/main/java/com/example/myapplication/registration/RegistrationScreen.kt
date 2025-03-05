package com.example.myapplication.registration

import android.widget.Toast
import androidx.compose.foundation.border
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

@Composable
fun RegistrationScreen() {
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var goalWeight by rememberSaveable { mutableStateOf("") }

    val genderOptions = listOf("Мужской", "Женский", "Другое")
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

        RegistrationInputField(value = email, onValueChange = { email = it }, label = "Email", keyboardType = KeyboardType.Email)
        RegistrationInputField(value = name, onValueChange = { name = it }, label = "Имя")

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            DropdownMenuBox(
                options = genderOptions,
                selected = selectedGender,
                onSelectionChange = { selectedGender = it }
            )
        }

        RegistrationInputField(value = age, onValueChange = { age = it }, label = "Возраст, лет", keyboardType = KeyboardType.Number)
        RegistrationInputField(value = weight, onValueChange = { weight = it }, label = "Вес, кг", keyboardType = KeyboardType.Number)
        RegistrationInputField(value = height, onValueChange = { height = it }, label = "Рост, см", keyboardType = KeyboardType.Number)
        RegistrationInputField(value = goalWeight, onValueChange = { goalWeight = it }, label = "Цель, кг", keyboardType = KeyboardType.Number)

        Button(
            onClick = {
                Toast.makeText(context, "клик", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Войти", color = MaterialTheme.colorScheme.inversePrimary)
        }
    }
}

@Composable
fun RegistrationInputField(
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
            .padding(bottom = 8.dp),
    )
}

@Composable
fun DropdownMenuBox(options: List<String>, selected: String?, onSelectionChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(0.5.dp, MaterialTheme.colorScheme.inversePrimary)
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
}