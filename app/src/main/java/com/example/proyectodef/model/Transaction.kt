package com.example.proyectodef.model

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val tipo: String = "",
    val fecha: Long = 0L,
    val titulo: String = "",
    val cantidad: Double = 0.0,
    val descripcion: String = "",
    val categoria: String = ""
)
