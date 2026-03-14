package com.example.encuestaapp.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String? = null
)