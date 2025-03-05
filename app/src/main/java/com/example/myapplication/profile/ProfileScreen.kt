package com.example.myapplication.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

object AppColors {
    val GrayText = Color(0xFF898989)
    val LightGray = Color.LightGray
}
object AppDimens {
    val ProfileImageSize = 200.dp
    val InputFontSize = 19.sp
    val ButtonPadding = 16.dp
}
@Composable
fun ProfileScreen() {
    var name by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.ButtonPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProfileImage()
        Spacer(modifier = Modifier.height(16.dp))
        ProfileText(text = if (name.isNotBlank()) name else "Имя", fontSize = 32.sp)
        ProfileText(text = if (height.isNotBlank()) "Рост: ${height} см" else "Рост в см", fontSize = 14.sp)
        ProfileText(text = if (weight.isNotBlank()) "Вес: ${weight} кг" else "Вес в кг", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        ProfileInputField(value = name, onValueChange = { name = it }, label = "Введите имя")
        ProfileInputField(value = height, onValueChange = { if (it.all { char -> char.isDigit() }) height = it }, label = "Введите рост", keyboardType = KeyboardType.Number)
        ProfileInputField(value = weight, onValueChange = { if (it.all { char -> char.isDigit() }) weight = it }, label = "Введите вес", keyboardType = KeyboardType.Number)

//        Button(
//            onClick = { },
//            modifier = Modifier.padding(top = AppDimens.ButtonPadding)
//        ) {
//            Text("Сохранить")
//        }
    }
}

@Composable
fun ProfileImage() {
    Image(
        painter = painterResource(id = R.drawable.cat_image),
        contentDescription = "Profile Image",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(AppDimens.ProfileImageSize)
            .clip(CircleShape)
            .background(AppColors.LightGray)
    )
}

@Composable
fun ProfileText(text: String, fontSize: TextUnit, fontWeight: FontWeight = FontWeight.Normal) {
    Text(
        text = text,
        color = AppColors.GrayText,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = MaterialTheme.colorScheme.inversePrimary) },
        textStyle = TextStyle(color = AppColors.GrayText, fontSize = AppDimens.InputFontSize),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    )
}
