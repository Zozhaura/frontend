package com.example.myapplication.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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

object AppColors {
    val DarkBackground = Color(0xFF494358)
    val DarkCard = Color(0xFF3A3A3A)
    val OrangeAccent = Color(0xFFFFA500)
    val LightGrayBorder = Color(0xFFD1C4E9)
    val SemiTransparentWhite = Color(0x9AFFFFFF)
    val LoadingColor = Color(0xFF9575CD)
}

object AppDimens {
    val ProfileImageSize = 200.dp
    val InputFontSize = 19.sp
    val ButtonPadding = 16.dp
    val CardPadding = 8.dp
    val CornerRadius = 16.dp
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val userResponse by remember { derivedStateOf { viewModel.userResponse } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var heightError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppDimens.ButtonPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImageSection(
                    avatarUri = avatarUri,
                    onEditClick = { pickImageLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.CardPadding),
                    shape = RoundedCornerShape(AppDimens.CornerRadius),
                    color = AppColors.DarkCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileText(
                            text = if (name.isNotBlank()) name else "Имя не указано",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.CardPadding),
                    shape = RoundedCornerShape(AppDimens.CornerRadius),
                    color = AppColors.DarkCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInputField(
                            value = name,
                            onValueChange = { newValue ->
                                name = newValue
                                nameError = newValue.isNotEmpty() && !(newValue.all { it.isLetter() || it.isWhitespace() } && newValue.length <= 50)
                            },
                            label = "Имя",
                            isError = nameError,
                            errorMessage = "Только буквы, до 50 символов"
                        )
                        ProfileInputField(
                            value = height,
                            onValueChange = { newValue ->
                                height = newValue
                                heightError = newValue.isNotEmpty() && !(newValue.all { it.isDigit() } && newValue.toIntOrNull() in 0..300)
                            },
                            label = "Рост (см)",
                            keyboardType = KeyboardType.Number,
                            isError = heightError,
                            errorMessage = "0-300 см"
                        )
                        ProfileInputField(
                            value = weight,
                            onValueChange = { newValue ->
                                weight = newValue
                                weightError = newValue.isNotEmpty() && !(newValue.all { it.isDigit() } && newValue.toIntOrNull() in 0..500)
                            },
                            label = "Вес (кг)",
                            keyboardType = KeyboardType.Number,
                            isError = weightError,
                            errorMessage = "0-500 кг"
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        TokenManager.clearToken(context)
                        Toast.makeText(context, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.CardPadding, vertical = AppDimens.ButtonPadding)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent),
                    shape = RoundedCornerShape(AppDimens.CornerRadius)
                ) {
                    Text(
                        "Выйти из аккаунта",
                        color = AppColors.SemiTransparentWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }
}

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
                .size(40.dp)
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
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label, color = AppColors.SemiTransparentWhite) },
            textStyle = TextStyle(color = AppColors.SemiTransparentWhite, fontSize = AppDimens.InputFontSize),
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
            isError = isError
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}