package com.example.proyectodef.model

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val tipo: String = "", // "Ahorro" o "Gasto"
    val fecha: Long = 0L, // timestamp
    val titulo: String = "",
    val cantidad: Double = 0.0,
    val descripcion: String = "",
    val categoria: String = ""
)
