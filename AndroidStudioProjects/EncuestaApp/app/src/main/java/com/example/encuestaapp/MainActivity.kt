package com.example.encuestaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.encuestaapp.ui.survey.SurveyScreen
import com.example.encuestaapp.ui.theme.EncuestaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EncuestaAppTheme {
                SurveyScreen()
            }
        }
    }
}