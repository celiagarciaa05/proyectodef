package com.example.proyectodef.model

data class User(
    val userId: String = "",
    val nombreCompleto: String = "",
    val nombreUsuario: String = "",
    val correo: String = "",
    val dineroTotal: Double = 0.0,
    val password: String = "",
    val photoUrl: String? = null
)
