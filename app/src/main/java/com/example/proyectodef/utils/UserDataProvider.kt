package com.example.proyectodef.utils

import com.example.proyectodef.model.Category
import com.example.proyectodef.model.Meta
import com.example.proyectodef.model.Transaction
import java.util.Date

object UserDataProvider {

    fun generarResumenFinanciero(
        nombre: String,
        correo: String,
        dineroTotal: Double,
        transacciones: List<Transaction>,
        categorias: List<Category>,
        metas: List<Meta>
    ): String {
        return buildString {
            appendLine("Usuario: $nombre")
            appendLine("Correo: $correo")
            appendLine("Dinero disponible: $dineroTotal €")
            appendLine("Categorías: ${categorias.joinToString { it.nombre }}")
            appendLine("Metas:")
            metas.forEach {
                appendLine("- ${it.tipo} ${it.categoria} ${it.cantidad}€ hasta ${Date(it.fechaLimite)} (estado: ${it.estado})")
            }
            appendLine("Transacciones recientes:")
            transacciones.takeLast(10).forEach {
                appendLine("- ${it.titulo}: ${it.cantidad}€ en ${it.categoria} (${it.tipo})")
            }
        }
    }
}