package com.example.myapplication.food

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FoodItemData(
    val id: Int,
    val name: String,
    val calories: String,
    val image: String,
    val defaultCount: Int
)

object FoodColors {
    val CardBackground = Color(0xFF494358)
    val TextColor = Color.LightGray
    val ButtonColor = Color(0xFF494358)
    val DividerColor = Color(0xFF6A637A)
    val SearchBackground = Color(0xFF5E566F)
}

object FoodDimens {
    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val CardCornerRadius = 16.dp
    val ButtonHeight = 56.dp
    val FoodItemPadding = 16.dp
    val DividerThickness = 1.dp
    val CounterSize = 32.dp
    val SearchHeight = 50.dp
    val FoodImageSize = 80.dp
    val NutrientCounterWidth = 100.dp
}

@Composable
fun FoodScreen() {
    val context = LocalContext.current
    var foodItems by remember { mutableStateOf<List<FoodItemData>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        foodItems = loadFoodItems(context)
    }

    val filteredItems = remember(foodItems, searchQuery) {
        if (searchQuery.isBlank()) foodItems
        else foodItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FoodDimens.PaddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Введите название блюда",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = FoodColors.TextColor,
                modifier = Modifier.padding(bottom = FoodDimens.PaddingSmall)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FoodDimens.SearchHeight),
                shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FoodColors.SearchBackground,
                    unfocusedContainerColor = FoodColors.SearchBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = { Text("Введите название блюда", color = FoodColors.TextColor) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = FoodColors.TextColor
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        Spacer(modifier = Modifier.height(FoodDimens.PaddingMedium))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
            color = FoodColors.CardBackground
        ) {
            Column {
                filteredItems.forEachIndexed { index, item ->
                    var count by remember { mutableIntStateOf(item.defaultCount) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FoodDimens.FoodItemPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = getImageResource(item.image)),
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(FoodDimens.FoodImageSize)
                                    .padding(end = FoodDimens.PaddingMedium)
                            )

                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FoodColors.TextColor
                                )
                                Text(
                                    text = item.calories,
                                    fontSize = 14.sp,
                                    color = FoodColors.TextColor
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { if (count > 0) count-- },
                                modifier = Modifier.size(FoodDimens.CounterSize)
                            ) {
                                Text("-", fontSize = 22.sp, color = FoodColors.TextColor)
                            }
                            Text(
                                count.toString(),
                                fontSize = 18.sp,
                                color = FoodColors.TextColor,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { count++ },
                                modifier = Modifier.size(FoodDimens.CounterSize)
                            ) {
                                Text("+", fontSize = 22.sp, color = FoodColors.TextColor)
                            }
                        }
                    }

                    if (index != filteredItems.size - 1) {
                        HorizontalDivider(
                            thickness = FoodDimens.DividerThickness,
                            color = FoodColors.DividerColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(FoodDimens.PaddingMedium))

        Button(
            onClick = {
                // кнопка для добавления блюд
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(FoodDimens.ButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = FoodColors.ButtonColor)
        ) {
            Text("ДОБАВИТЬ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(FoodDimens.PaddingMedium))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(FoodDimens.CardCornerRadius),
            color = FoodColors.CardBackground
        ) {
            Column(
                modifier = Modifier.padding(FoodDimens.PaddingMedium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добавить своё блюдо",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FoodColors.TextColor,
                    modifier = Modifier.padding(bottom = FoodDimens.PaddingMedium)
                )

                var protein by remember { mutableIntStateOf(6) }
                var fat by remember { mutableIntStateOf(8) }
                var carbs by remember { mutableIntStateOf(12) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NutrientCounter(
                            value = protein,
                            onDecrease = { if (protein > 0) protein-- },
                            onIncrease = { protein++ }
                        )
                        NutrientCounter(
                            value = fat,
                            onDecrease = { if (fat > 0) fat-- },
                            onIncrease = { fat++ }
                        )
                        NutrientCounter(
                            value = carbs,
                            onDecrease = { if (carbs > 0) carbs-- },
                            onIncrease = { carbs++ }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("БЕЛКИ", color = FoodColors.TextColor)
                        Text("ЖИРЫ", color = FoodColors.TextColor)
                        Text("УГЛЕВОДЫ", color = FoodColors.TextColor)
                    }

                    Spacer(modifier = Modifier.height(FoodDimens.PaddingSmall))

                    Button(
                        onClick = {
                            // кнопка для своего блюда
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FoodDimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodColors.ButtonColor)
                    ) {
                        Text("ДОБАВИТЬ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientCounter(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(FoodDimens.NutrientCounterWidth),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(FoodDimens.CounterSize)
        ) {
            Text("-", fontSize = 18.sp, color = FoodColors.TextColor)
        }

        Text("$value г", fontSize = 16.sp, color = FoodColors.TextColor)

        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(FoodDimens.CounterSize)
        ) {
            Text("+", fontSize = 18.sp, color = FoodColors.TextColor)
        }
    }
}

private fun getImageResource(imageName: String): Int {
    return when (imageName) {
        "pizza" -> R.drawable.pizza
        "cake" -> R.drawable.cake
        "banana" -> R.drawable.banana
        else -> R.drawable.shrek
    }
}

private fun loadFoodItems(context: Context): List<FoodItemData> {
    return try {
        context.resources.openRawResource(R.raw.food_data).use { stream ->
            Gson().fromJson(
                stream.bufferedReader().use { it.readText() },
                object : TypeToken<List<FoodItemData>>() {}.type
            ) ?: emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}