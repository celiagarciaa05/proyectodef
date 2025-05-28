package com.example.proyectodef.model

data class User(
    val userId: String = "",           // ID único, puede venir de Google o de tu backend
    val nombreCompleto: String = "",
    val nombreUsuario: String = "",
    val correo: String = "",
    val dineroTotal: Double = 0.0,
    val password: String = "",
    val photoUrl: String? = null       // Opcional, si usas foto de perfil de Google
)
