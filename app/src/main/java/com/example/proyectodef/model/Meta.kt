package com.example.proyectodef.model

data class Meta(
    val id: String = "",
    val userId: String = "",
    val categoria: String = "",
    val tipo: String = "", // "Ahorro" o "Gasto"
    val cantidad: Double = 0.0,
    val fechaLimite: Long = 0L,
    val fechaCreacion: Long = System.currentTimeMillis(),// timestamp
    val estado: String = "Proceso",
    val progreso: Float = 0f // "Proceso", "Completado", "Expirado"
)
