package com.example.encuestaapp.ui.survey

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.encuestaapp.ui.theme.PitchGreen

@Composable
fun SurveyScreen(viewModel: SurveyViewModel = viewModel()) {
    var showStartScreen by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Fondo global para todas las pantallas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PitchGreen.copy(alpha = 0.8f),
                        PitchGreen
                    )
                )
            )
    ) {
        if (showStartScreen) {
            StartScreen(
                onStartClick = { showStartScreen = false },
                onExitClick = { (context as? ComponentActivity)?.finish() }
            )
        } else if (viewModel.showStats) {
            StatsScreen(viewModel)
        } else if (viewModel.isSurveyFinished) {
            FinishScreen(viewModel)
        } else {
            AllQuestionsScreen(viewModel)
        }
    }
}

@Composable
fun StartScreen(onStartClick: () -> Unit, onExitClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚽", fontSize = 100.sp)
        Text(
            text = "¡BIENVENIDO HINCHA!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Queremos conocer tu opinión sobre el deporte más lindo del mundo.",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
        )

        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PitchGreen),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("INICIAR ENCUESTA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onExitClick,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("MEJOR EN OTRO MOMENTO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AllQuestionsScreen(viewModel: SurveyViewModel) {
    val responses = viewModel.userResponses

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "La Voz del Hincha",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Porfavor responda segun su criterio.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(viewModel.questions) { index, question ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        QuestionItem(
                            question = question,
                            selectedAnswer = responses[index] ?: "",
                            onAnswerSelected = { viewModel.saveAnswer(index, it) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendResultsToFirestore() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = viewModel.isAllAnswered() && !viewModel.isSending,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = PitchGreen,
                disabledContainerColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            if (viewModel.isSending) {
                CircularProgressIndicator(color = PitchGreen, modifier = Modifier.size(24.dp))
            } else {
                Text("ENVIAR RESPUESTAS", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuestionItem(
    question: QuestionType,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = question.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when (question) {
            is QuestionType.SingleChoice -> {
                var customText by remember { mutableStateOf("") }
                val isOtroSelected = selectedAnswer != "" && !question.options.contains(selectedAnswer) || selectedAnswer == "Otro"
                
                question.options.forEach { option ->
                    val isSelected = if (option == "Otro") isOtroSelected else selectedAnswer == option

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { 
                                    if (option == "Otro") onAnswerSelected("Otro") else onAnswerSelected(option)
                                }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { 
                                if (option == "Otro") onAnswerSelected("Otro") else onAnswerSelected(option)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = PitchGreen)
                        )
                        Text(text = option, color = Color.DarkGray, modifier = Modifier.padding(start = 8.dp), fontSize = 16.sp)
                    }
                }

                if (isOtroSelected) {
                    OutlinedTextField(
                        value = if (selectedAnswer == "Otro") customText else selectedAnswer,
                        onValueChange = { 
                            customText = it
                            onAnswerSelected(it) 
                        },
                        label = { Text("Nombre del jugador/club") },
                        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, top = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchGreen,
                            focusedLabelColor = PitchGreen
                        ),
                        singleLine = true
                    )
                }
            }
            is QuestionType.InputText -> {
                OutlinedTextField(
                    value = selectedAnswer,
                    onValueChange = { onAnswerSelected(it) },
                    label = { Text("Escribe tu respuesta") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PitchGreen,
                        focusedLabelColor = PitchGreen
                    )
                )
            }
        }
    }
}

@Composable
fun FinishScreen(viewModel: SurveyViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚽", fontSize = 80.sp)
        Text("¡GOLAZO!", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(
            "Tu encuesta ha sido enviada con éxito.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = { viewModel.showStats = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PitchGreen)
        ) {
            Text("VER ESTADÍSTICAS", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatsScreen(viewModel: SurveyViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchStats()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "Estadísticas del Estadio",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total de Personas que Respondieron", color = Color.White, fontSize = 14.sp)
                Text("${viewModel.totalSurveys} / 60", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(viewModel.questions) { index, question ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val questionStats = viewModel.statsMap[index] ?: emptyMap()
                        
                        if (questionStats.isEmpty()) {
                            Text("Cargando datos...", fontSize = 12.sp, color = Color.Gray)
                        }

                        questionStats.forEach { (answer, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(PitchGreen.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(answer, modifier = Modifier.weight(1f), color = Color.DarkGray)
                                Text(text = "$count votos", fontWeight = FontWeight.Bold, color = PitchGreen)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.resetSurvey() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = viewModel.totalSurveys < 60,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
        ) {
            Text(
                if (viewModel.totalSurveys < 60) "VOLVER A JUGAR" else "LÍMITE ALCANZADO",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
