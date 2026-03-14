package com.example.encuestaapp.ui.survey

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

// Definición de tipos de preguntas con ID único
sealed class QuestionType(val id: String, val title: String) {
    class SingleChoice(id: String, t: String, val options: List<String>) : QuestionType(id, t)
    class InputText(id: String, t: String) : QuestionType(id, t)
}

class SurveyViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Lista de preguntas con opciones actualizadas
    val questions = listOf(
        QuestionType.SingleChoice("posicion", "¿En qué posición juegas?", listOf("Portero", "Defensa", "Mediocentro", "Delantero")),
        QuestionType.SingleChoice("torneo", "¿Tu torneo favorito?", listOf("LaLiga", "Champions League", "Premier", "Copa del Mundo", "Serie A")),
        QuestionType.SingleChoice(
            "mejor_actual", 
            "¿Quién es el mejor jugador actual?", 
            listOf("Kylian Mbappé", "Vinícius Jr", "Erling Haaland", "Jude Bellingham", "Lamine Yamal", "Otro")
        ),
        QuestionType.SingleChoice(
            "mejor_historia",
            "¿Para ti quién es el mejor jugador de la historia?",
            listOf("Lionel Messi", "Cristiano Ronaldo", "Pelé", "Diego Maradona", "Johan Cruyff", "Otro")
        ),
        QuestionType.SingleChoice("var_opinion", "¿Te gusta el uso del VAR?", listOf("Sí, ayuda mucho", "No, quita esencia", "Solo en jugadas clave")),
        QuestionType.SingleChoice(
            "frecuencia", 
            "¿Con qué frecuencia juegas fútbol?", 
            listOf("Todos los días", "Varias veces por semana", "Solo fines de semana", "Casi nunca")
        ),
        QuestionType.SingleChoice("jugar_o_ver", "¿Prefieres jugar o ver fútbol?", listOf("Jugar", "Ver partidos", "Ambos por igual")),
        QuestionType.SingleChoice(
            "club_favorito", 
            "¿Cuál es tu club favorito?", 
            listOf("Barcelona", "Real Madrid", "Manchester City", "Bayern Munich", "Otro")
        ),
        QuestionType.SingleChoice("clubes_vs_selecciones", "¿Prefieres clubes o selecciones?", listOf("Clubes", "Selecciones nacionales")),
        QuestionType.SingleChoice(
            "donde_ver", 
            "¿Dónde prefieres ver los partidos?", 
            listOf("En casa", "En un bar o restaurante", "En el estadio", "En el celular")
        )
    )

    private val _userResponses = mutableStateMapOf<Int, String>()
    val userResponses: Map<Int, String> get() = _userResponses

    var isSurveyFinished by mutableStateOf(false)
    var isSending by mutableStateOf(false)
    var showStats by mutableStateOf(false)
    var currentStep by mutableIntStateOf(0)

    var totalSurveys by mutableIntStateOf(0)
    val statsMap = mutableStateMapOf<Int, Map<String, Int>>()

    fun saveAnswer(index: Int, answer: String) {
        _userResponses[index] = answer
    }

    fun isAllAnswered(): Boolean {
        return questions.indices.all { index ->
            val response = _userResponses[index]
            !response.isNullOrBlank() && response != "Otro"
        }
    }

    fun resetSurvey() {
        _userResponses.clear()
        isSurveyFinished = false
        showStats = false
        currentStep = 0
    }

    fun sendResultsToFirestore() {
        if (totalSurveys >= 60) {
            isSurveyFinished = true
            return
        }

        isSending = true
        val data = mutableMapOf<String, Any>()
        _userResponses.forEach { (index, respuesta) ->
            data[questions[index].id] = respuesta
        }
        data["timestamp"] = System.currentTimeMillis()

        db.collection("encuestas_futbol_v2")
            .add(data)
            .addOnSuccessListener {
                isSending = false
                isSurveyFinished = true
                fetchStats()
            }
            .addOnFailureListener {
                isSending = false
                isSurveyFinished = true
                fetchStats()
            }
    }

    fun fetchStats() {
        db.collection("encuestas_futbol_v2")
            .get()
            .addOnSuccessListener { result ->
                totalSurveys = result.size()
                val newStats = mutableMapOf<Int, MutableMap<String, Int>>()
                
                for (document in result) {
                    for (i in questions.indices) {
                        val answer = document.getString(questions[i].id)
                        if (answer != null) {
                            val questionStats = newStats.getOrPut(i) { mutableMapOf() }
                            questionStats[answer] = (questionStats[answer] ?: 0) + 1
                        }
                    }
                }
                
                statsMap.clear()
                newStats.forEach { (index, map) ->
                    statsMap[index] = map
                }
            }
    }
}
