package com.example.gestprom.models

import com.google.firebase.firestore.PropertyName

data class Semestre(
    val id: String = "",
    val nombre: String = "",
    @PropertyName("configuracionEvaluaciones")
    val configuracionEvaluaciones: ConfiguracionEvaluaciones = ConfiguracionEvaluaciones()
)

data class ConfiguracionEvaluaciones(
    @PropertyName("parcial1_fecha")
    val parcial1_fecha: String = "",
    @PropertyName("parcial2_fecha")
    val parcial2_fecha: String = "",
    @PropertyName("parcial3_fecha")
    val parcial3_fecha: String = "",
    @PropertyName("ordinario_fecha")
    val ordinario_fecha: String = ""
) 