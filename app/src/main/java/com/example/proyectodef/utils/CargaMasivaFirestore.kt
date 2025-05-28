package com.example.proyectodef.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

object CargaMasivaFirestore {

    private val categorias = listOf("Comida", "Salud", "Trabajo", "Ocio")

    suspend fun insertarDatos(userId: String) {
        println("alabarran: Iniciando función insertarDatos para $userId")

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val transRef = db.collection("usuarios").document(userId).collection("transacciones")
        val metasRef = db.collection("usuarios").document(userId).collection("metas")
        val categoriasRef = db.collection("usuarios").document(userId).collection("categorias")
        val flagRef = db.collection("usuarios").document(userId).collection("banderas").document("datosPrueba")

        // Verificar si ya se hizo la carga
        val flagSnapshot = flagRef.get().await()
        if (flagSnapshot.exists() && flagSnapshot.getBoolean("datosCargados") == true) {
            println("alabarran: ⚠️ Los datos de prueba ya fueron insertados previamente.")
            return
        }

        val ahora = System.currentTimeMillis()
        val hace30Dias = ahora - 30L * 24 * 60 * 60 * 1000

        println("alabarran: Insertando transacciones de ahorro")
        repeat(6) { i ->
            val fecha = if (i < 5) randomFechaReciente(hace30Dias, ahora) else randomFechaAntigua()
            val data = mapOf(
                "descripcion" to "Ingreso $i",
                "cantidad" to Random.Default.nextDouble(50.0, 300.0),
                "categoria" to categorias.random(),
                "tipo" to "Ahorro",
                "fecha" to fecha
            )
            batch.set(transRef.document(), data)
        }

        println("alabarran: Insertando transacciones de gasto")
        repeat(9) { i ->
            val fecha = if (i < 6) randomFechaReciente(hace30Dias, ahora) else randomFechaAntigua()
            val data = mapOf(
                "descripcion" to "Gasto $i",
                "cantidad" to Random.Default.nextDouble(10.0, 150.0),
                "categoria" to categorias.random(),
                "tipo" to "Gasto",
                "fecha" to fecha
            )
            batch.set(transRef.document(), data)
        }

        println("alabarran: Insertando metas")
        repeat(9) { i ->
            val tipo = if (i < 6) "Ahorro" else "Gasto"
            val estado = when {
                i in 0..2 -> "Completado"
                i == 3 -> "Expirado"
                else -> "Proceso"
            }
            val data = mapOf(
                "categoria" to categorias.random(),
                "tipo" to tipo,
                "cantidad" to Random.Default.nextDouble(100.0, 1000.0),
                "fechaLimite" to randomFechaReciente(hace30Dias, ahora),
                "estado" to estado,
                "userId" to userId
            )
            batch.set(metasRef.document(), data)
        }

        println("alabarran: Insertando categorías")
        categorias.forEach { nombre ->
            val data = mapOf(
                "nombre" to nombre,
                "presupuesto" to Random.Default.nextDouble(200.0, 800.0)
            )
            batch.set(categoriasRef.document(), data)
        }

        // Marcar como cargado
        batch.set(flagRef, mapOf("datosCargados" to true))
        println("alabarran: Guardando bandera de datosCargados")

        try {
            batch.commit().await()
            println("alabarran: ✅ Datos de prueba insertados y marcados como cargados.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("alabarran: ❌ Error al insertar datos: ${e.message}")
        }
    }

    private fun randomFechaReciente(inicio: Long, fin: Long): Long {
        return (inicio..fin).random()
    }

    private fun randomFechaAntigua(): Long {
        val ahora = System.currentTimeMillis()
        return ahora - Random.Default.nextLong(60L * 24 * 60 * 60 * 1000, 180L * 24 * 60 * 60 * 1000)
    }
}