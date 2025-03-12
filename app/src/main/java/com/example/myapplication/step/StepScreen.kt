package com.example.myapplication.step

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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StepScreen(
    viewModel: StepViewModel = viewModel()
) {
    val currentSteps = viewModel.stepsState.collectAsState()
    val currentCalories = viewModel.caloriesState.collectAsState()

    var maxSteps by remember { mutableStateOf(10000) }

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
                progress = 1f,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                color = Color.LightGray,
                strokeWidth = 4.dp,
            )
            CircularProgressIndicator(
                progress = (currentSteps.value.toFloat() / maxSteps).coerceIn(0f, 1f),
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
                    text = "${currentSteps.value} шагов",
                    color = Color(0x9AFFFFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${maxSteps - currentSteps.value} осталось",
                    color = Color(0x9AFFFFFF),
                    fontSize = 16.sp
                )
            }
        }

        Text(
            text = "Калорий потрачено: ${currentCalories.value}",
            color = Color(0x9AFFFFFF),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                    text = "Выберите цель",
                    color = Color(0x9AFFFFFF),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StepButton("4000") { maxSteps = 4000 }
                    StepButton("6000") { maxSteps = 6000 }
                    StepButton("8000") { maxSteps = 8000 }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StepButton("10000") { maxSteps = 10000 }
                    StepButton("12000") { maxSteps = 12000 }
                    StepButton("15000") { maxSteps = 15000 }
                }
            }
        }
    }
}

@Composable
fun StepButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text)
    }
}
