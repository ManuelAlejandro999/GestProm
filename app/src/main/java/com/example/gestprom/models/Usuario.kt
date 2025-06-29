package com.example.gestprom.models

data class Usuario(
    val id: String = "",
    val nombreCompleto: String = "",
    val matricula: String = "",
    val correo: String = "",
    val contraseñaHash: String = ""
) 