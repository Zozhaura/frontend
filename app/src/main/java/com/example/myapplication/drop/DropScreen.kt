package com.example.myapplication.drop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DropScreen() {
    var currentWater by remember { mutableStateOf(0) }
    val maxWater = 2000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 24.dp)
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                color = Color(0x90A4A4A4),
                strokeWidth = 4.dp,
            )

            CircularProgressIndicator(
                progress = { (currentWater.toFloat() / maxWater).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                color = Color(0xFFFFA500),
                strokeWidth = 8.dp,
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currentWater мл",
                    color = Color(0x9AFFFFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${maxWater - currentWater} мл осталось",
                    color = Color(0x9AFFFFFF),
                    fontSize = 16.sp
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF494358)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выберите объем порции",
                    color = Color(0x9AFFFFFF),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WaterButton("50 мл") {
                        if (currentWater + 50 <= maxWater) currentWater += 50
                    }
                    WaterButton("100 мл") {
                        if (currentWater + 100 <= maxWater) currentWater += 100
                    }
                    WaterButton("150 мл") {
                        if (currentWater + 150 <= maxWater) currentWater += 150
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WaterButton("200 мл") {
                        if (currentWater + 200 <= maxWater) currentWater += 200
                    }
                    WaterButton("250 мл") {
                        if (currentWater + 250 <= maxWater) currentWater += 250
                    }
                    WaterButton("300 мл") {
                        if (currentWater + 300 <= maxWater) currentWater += 300
                    }
                }
            }
        }
    }
}

@Composable
fun WaterButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .width(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text)
    }
}