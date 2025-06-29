package com.example.gestprom.models

// Data class para las evaluaciones
data class Evaluacion(
    val id: String = "",
    val nombre: String = "",
    val fecha: String = "",
    val calificacionResultado: Double = 0.0,
    val fechaRegistro: String = ""
)